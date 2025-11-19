package gov.lby.cityissuetracker.kafka;

import gov.lby.cityissuetracker.kafka.event.IssueEvent;
import gov.lby.cityissuetracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IssueEventConsumer {

    private final DashboardService dashboardService;

    @KafkaListener(
            topics = KafkaTopicConfig.ISSUE_EVENTS_TOPIC,
            groupId = "dashboard-sse-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeIssueEvent(IssueEvent event) {
        log.info("Received issue event: {} for issue {}", event.getEventType(), event.getIssueId());

        // Broadcast to all connected SSE clients
        dashboardService.broadcastEvent(event);
    }
}
