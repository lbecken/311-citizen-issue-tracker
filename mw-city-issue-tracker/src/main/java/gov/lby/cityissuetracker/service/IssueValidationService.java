package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.entity.Issue;
import gov.lby.cityissuetracker.entity.IssueCategory;
import gov.lby.cityissuetracker.exception.IssueNotFoundException;
import gov.lby.cityissuetracker.messaging.IssueMessagePublisher;
import gov.lby.cityissuetracker.repository.IssueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.locationtech.jts.geom.Point;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IssueValidationService {

    private final IssueRepository issueRepository;
    private final IssueMessagePublisher messagePublisher;

    // Radius in meters for duplicate detection
    private static final double DUPLICATE_RADIUS_METERS = 100.0;
    // Days to look back for duplicate detection
    private static final int DUPLICATE_LOOKBACK_DAYS = 7;
    // Threshold for auto-priority upgrade
    private static final int DUPLICATE_THRESHOLD_FOR_UPGRADE = 3;

    public void validateAndPrioritize(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IssueNotFoundException("Issue not found: " + issueId));

        log.info("Validating issue: {} with category: {}", issueId, issue.getCategory());

        // Check for duplicates
        List<Issue> duplicates = findDuplicateIssues(issue);
        boolean isDuplicate = !duplicates.isEmpty();
        int duplicateCount = duplicates.size();

        log.info("Found {} potential duplicate issues for issue: {}", duplicateCount, issueId);

        // Calculate priority based on duplicates and category
        int newPriority = calculatePriority(issue, duplicateCount);

        if (newPriority != issue.getPriority()) {
            log.info("Updating priority for issue {} from {} to {}", issueId, issue.getPriority(), newPriority);
            issue.setPriority(newPriority);
            issueRepository.save(issue);
        }

        // Publish validated event for assignment queue
        messagePublisher.publishValidatedIssue(issueId, newPriority, isDuplicate);
    }

    private List<Issue> findDuplicateIssues(Issue issue) {
        Point location = issue.getLocation();
        IssueCategory category = issue.getCategory();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DUPLICATE_LOOKBACK_DAYS);

        // Find issues within radius, same category, and within lookback period
        // Excluding the current issue
        return issueRepository.findDuplicateIssues(
                location,
                DUPLICATE_RADIUS_METERS,
                category,
                cutoffDate,
                issue.getId()
        );
    }

    private int calculatePriority(Issue issue, int duplicateCount) {
        int basePriority = issue.getPriority();

        // Auto-upgrade priority if there are many duplicates
        if (duplicateCount >= DUPLICATE_THRESHOLD_FOR_UPGRADE) {
            // Each 3 duplicates increases priority by 1 (lower number = higher priority)
            int priorityBoost = duplicateCount / DUPLICATE_THRESHOLD_FOR_UPGRADE;
            int newPriority = Math.max(1, basePriority - priorityBoost); // Priority can't go below 1

            log.info("Auto-upgrading priority due to {} duplicates: {} -> {}",
                    duplicateCount, basePriority, newPriority);

            return newPriority;
        }

        // Category-based priority adjustments for safety-related issues
        switch (issue.getCategory()) {
            case POTHOLE:
            case STREETLIGHT:
                return Math.max(1, basePriority - 1); // Higher priority for safety-related
            default:
                return basePriority;
        }
    }
}
