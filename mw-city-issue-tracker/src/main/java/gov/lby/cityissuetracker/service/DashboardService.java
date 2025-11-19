package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.entity.IssueCategory;
import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.kafka.event.DashboardMetrics;
import gov.lby.cityissuetracker.kafka.event.IssueEvent;
import gov.lby.cityissuetracker.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final IssueRepository issueRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public DashboardMetrics getMetrics() {
        // Get counts by status
        Map<String, Long> issuesByStatus = Arrays.stream(IssueStatus.values())
                .collect(Collectors.toMap(
                        IssueStatus::name,
                        status -> issueRepository.countByStatus(status)
                ));

        // Get counts by category
        Map<String, Long> issuesByCategory = Arrays.stream(IssueCategory.values())
                .collect(Collectors.toMap(
                        IssueCategory::name,
                        category -> issueRepository.countByCategory(category)
                ));

        // Get counts by priority
        Map<String, Long> issuesByPriority = Map.of(
                "1", issueRepository.countByPriority(1),
                "2", issueRepository.countByPriority(2),
                "3", issueRepository.countByPriority(3),
                "4", issueRepository.countByPriority(4),
                "5", issueRepository.countByPriority(5)
        );

        long totalIssues = issueRepository.count();
        long openIssues = issuesByStatus.getOrDefault(IssueStatus.REPORTED.name(), 0L)
                + issuesByStatus.getOrDefault(IssueStatus.VALIDATED.name(), 0L)
                + issuesByStatus.getOrDefault(IssueStatus.ASSIGNED.name(), 0L)
                + issuesByStatus.getOrDefault(IssueStatus.IN_PROGRESS.name(), 0L);
        long resolvedIssues = issuesByStatus.getOrDefault(IssueStatus.RESOLVED.name(), 0L);
        long closedIssues = issuesByStatus.getOrDefault(IssueStatus.CLOSED.name(), 0L);

        return DashboardMetrics.builder()
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .resolvedIssues(resolvedIssues)
                .closedIssues(closedIssues)
                .issuesByStatus(issuesByStatus)
                .issuesByCategory(issuesByCategory)
                .issuesByPriority(issuesByPriority)
                .timestamp(Instant.now())
                .build();
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE client disconnected");
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE client timed out");
        });

        emitter.onError(e -> {
            emitters.remove(emitter);
            log.debug("SSE error: {}", e.getMessage());
        });

        // Send initial metrics
        try {
            emitter.send(SseEmitter.event()
                    .name("metrics")
                    .data(getMetrics()));
        } catch (IOException e) {
            log.error("Error sending initial metrics", e);
            emitters.remove(emitter);
        }

        log.debug("New SSE client subscribed, total clients: {}", emitters.size());
        return emitter;
    }

    public void broadcastEvent(IssueEvent event) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("issue-event")
                        .data(event));

                // Also send updated metrics
                emitter.send(SseEmitter.event()
                        .name("metrics")
                        .data(getMetrics()));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
        if (!deadEmitters.isEmpty()) {
            log.debug("Removed {} dead SSE emitters", deadEmitters.size());
        }
    }
}
