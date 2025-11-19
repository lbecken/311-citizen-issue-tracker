package gov.lby.cityissuetracker.kafka;

import gov.lby.cityissuetracker.entity.IssueCategory;
import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.kafka.event.IssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueKafkaProducer {

    private final KafkaTemplate<String, IssueEvent> kafkaTemplate;

    public void sendIssueCreated(UUID issueId, IssueStatus status, IssueCategory category, Integer priority) {
        IssueEvent event = IssueEvent.created(issueId, status, category, priority);
        sendEvent(issueId.toString(), event);
    }

    public void sendStatusChange(UUID issueId, IssueStatus previousStatus, IssueStatus newStatus,
                                 IssueCategory category, Integer priority) {
        IssueEvent event = IssueEvent.statusChanged(issueId, previousStatus, newStatus, category, priority);
        sendEvent(issueId.toString(), event);
    }

    private void sendEvent(String key, IssueEvent event) {
        CompletableFuture<SendResult<String, IssueEvent>> future = kafkaTemplate.send(
                KafkaTopicConfig.ISSUE_EVENTS_TOPIC,
                key,
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent issue event [{}] to topic [{}] with offset [{}]",
                        event.getEventType(),
                        KafkaTopicConfig.ISSUE_EVENTS_TOPIC,
                        result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send issue event [{}] to topic [{}]",
                        event.getEventType(),
                        KafkaTopicConfig.ISSUE_EVENTS_TOPIC,
                        ex);
            }
        });
    }
}
