package gov.lby.cityissuetracker.messaging;

import gov.lby.cityissuetracker.config.RabbitConfig;
import gov.lby.cityissuetracker.event.IssueReportedEvent;
import gov.lby.cityissuetracker.service.IssueValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IssueValidationConsumer {

    private final IssueValidationService validationService;

    @RabbitListener(queues = RabbitConfig.VALIDATION_QUEUE)
    public void handleValidation(IssueReportedEvent event) {
        log.info("Received IssueReportedEvent for issue: {}", event.getIssueId());

        try {
            validationService.validateAndPrioritize(event.getIssueId());
            log.info("Successfully validated issue: {}", event.getIssueId());
        } catch (Exception e) {
            log.error("Validation failed for issue: {}. Error: {}", event.getIssueId(), e.getMessage());
            // Send to dead letter queue instead of requeuing
            throw new AmqpRejectAndDontRequeueException("Validation failed for issue: " + event.getIssueId(), e);
        }
    }
}
