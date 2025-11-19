package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.entity.Issue;
import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.repository.IssueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolutionTimePredictor {

    private final IssueRepository issueRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Scheduled task that predicts resolution times for pending issues.
     * Runs every minute to update predictions.
     */
    @Scheduled(fixedDelay = 60000)
    public void predictResolutionTimes() {
        List<Issue> pendingIssues = issueRepository.findByStatusIn(
                List.of(IssueStatus.REPORTED, IssueStatus.VALIDATED)
        );

        log.info("Predicting resolution times for {} pending issues", pendingIssues.size());

        for (Issue issue : pendingIssues) {
            try {
                predictForIssue(issue);
            } catch (Exception e) {
                log.error("Failed to predict resolution time for issue {}: {}",
                        issue.getId(), e.getMessage());
            }
        }
    }

    private void predictForIssue(Issue issue) {
        // Get historical data for the department and category
        Double avgHours = getHistoricalAvgHours(issue);
        Double medianHours = getHistoricalMedianHours(issue);

        // If no historical data, use department's default
        if (avgHours == null) {
            avgHours = issue.getDepartment() != null
                    ? (double) issue.getDepartment().getAvgResolutionHours()
                    : 48.0;
        }
        if (medianHours == null) {
            medianHours = avgHours;
        }

        String prompt = """
                Predict resolution time in hours for this issue.

                Historical data for this dept/category:
                - Average: %.2f hours
                - Median: %.2f hours

                Current issue: %s
                Priority: %d/5

                Consider: higher priority = faster, simple issues = faster.
                Respond with just a number (hours).
                """.formatted(avgHours, medianHours, issue.getTitle(), issue.getPriority());

        try {
            ChatClient chatClient = chatClientBuilder.build();
            String prediction = chatClient.prompt(prompt).call().content();

            // Clean up the prediction
            prediction = prediction.trim().replaceAll("[^0-9.]", "");

            // Validate the prediction is a valid number
            double predictedHours = Double.parseDouble(prediction);
            if (predictedHours < 0 || predictedHours > 720) { // Max 30 days
                predictedHours = avgHours;
            }

            // Cache prediction in Redis for 1 hour
            String cacheKey = "prediction:" + issue.getId();
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(predictedHours), Duration.ofHours(1));

            log.debug("Predicted resolution time for issue {}: {} hours", issue.getId(), predictedHours);
        } catch (Exception e) {
            log.warn("AI prediction failed for issue {}, using historical average: {}",
                    issue.getId(), e.getMessage());
            // Cache the historical average as fallback
            String cacheKey = "prediction:" + issue.getId();
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(avgHours), Duration.ofHours(1));
        }
    }

    private Double getHistoricalAvgHours(Issue issue) {
        if (issue.getDepartment() == null) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT avg_hours FROM historical_resolution_times WHERE department_id = ? AND category = ?",
                    Double.class,
                    issue.getDepartment().getId(),
                    issue.getCategory().name()
            );
        } catch (Exception e) {
            return null;
        }
    }

    private Double getHistoricalMedianHours(Issue issue) {
        if (issue.getDepartment() == null) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT median_hours FROM historical_resolution_times WHERE department_id = ? AND category = ?",
                    Double.class,
                    issue.getDepartment().getId(),
                    issue.getCategory().name()
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the cached prediction for an issue.
     *
     * @param issueId The issue ID
     * @return The predicted resolution time in hours, or null if not cached
     */
    public Double getPrediction(String issueId) {
        String cacheKey = "prediction:" + issueId;
        String prediction = redisTemplate.opsForValue().get(cacheKey);
        return prediction != null ? Double.parseDouble(prediction) : null;
    }
}
