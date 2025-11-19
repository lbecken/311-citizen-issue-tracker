package gov.lby.cityissuetracker.messaging;

import gov.lby.cityissuetracker.config.RabbitConfig;
import gov.lby.cityissuetracker.event.IssueReportedEvent;
import gov.lby.cityissuetracker.event.IssueValidatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishNewIssue(UUID issueId) {
        IssueReportedEvent event = new IssueReportedEvent(issueId, Instant.now());

        log.info("Publishing IssueReportedEvent for issue: {}", issueId);

        rabbitTemplate.convertAndSend(
                RabbitConfig.ISSUE_EXCHANGE,
                RabbitConfig.ISSUE_REPORTED_ROUTING_KEY,
                event
        );
    }

    public void publishValidatedIssue(UUID issueId, Integer priority, boolean isDuplicate) {
        IssueValidatedEvent event = new IssueValidatedEvent(issueId, priority, isDuplicate, Instant.now());

        log.info("Publishing IssueValidatedEvent for issue: {} with priority: {}", issueId, priority);

        rabbitTemplate.convertAndSend(
                RabbitConfig.ISSUE_EXCHANGE,
                RabbitConfig.ISSUE_VALIDATED_ROUTING_KEY,
                event
        );
    }
}
