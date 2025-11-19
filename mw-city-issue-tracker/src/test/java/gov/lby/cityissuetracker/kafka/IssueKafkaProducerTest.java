package gov.lby.cityissuetracker.kafka;

import gov.lby.cityissuetracker.entity.IssueCategory;
import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.kafka.event.IssueEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, IssueEvent> kafkaTemplate;

    @Mock
    private SendResult<String, IssueEvent> sendResult;

    private IssueKafkaProducer producer;

    @BeforeEach
    void setUp() {
        producer = new IssueKafkaProducer(kafkaTemplate);
    }

    @Test
    void sendIssueCreated_shouldSendEventToKafka() {
        // Given
        UUID issueId = UUID.randomUUID();
        IssueStatus status = IssueStatus.REPORTED;
        IssueCategory category = IssueCategory.POTHOLE;
        Integer priority = 3;

        CompletableFuture<SendResult<String, IssueEvent>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendIssueCreated(issueId, status, category, priority);

        // Then
        ArgumentCaptor<IssueEvent> eventCaptor = ArgumentCaptor.forClass(IssueEvent.class);
        verify(kafkaTemplate).send(
                eq(KafkaTopicConfig.ISSUE_EVENTS_TOPIC),
                eq(issueId.toString()),
                eventCaptor.capture()
        );

        IssueEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getIssueId()).isEqualTo(issueId);
        assertThat(capturedEvent.getEventType()).isEqualTo(IssueEvent.EventType.CREATED);
        assertThat(capturedEvent.getStatus()).isEqualTo(status);
        assertThat(capturedEvent.getCategory()).isEqualTo(category);
        assertThat(capturedEvent.getPriority()).isEqualTo(priority);
        assertThat(capturedEvent.getTimestamp()).isNotNull();
    }

    @Test
    void sendStatusChange_shouldSendEventToKafka() {
        // Given
        UUID issueId = UUID.randomUUID();
        IssueStatus previousStatus = IssueStatus.REPORTED;
        IssueStatus newStatus = IssueStatus.IN_PROGRESS;
        IssueCategory category = IssueCategory.STREETLIGHT;
        Integer priority = 2;

        CompletableFuture<SendResult<String, IssueEvent>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendStatusChange(issueId, previousStatus, newStatus, category, priority);

        // Then
        ArgumentCaptor<IssueEvent> eventCaptor = ArgumentCaptor.forClass(IssueEvent.class);
        verify(kafkaTemplate).send(
                eq(KafkaTopicConfig.ISSUE_EVENTS_TOPIC),
                eq(issueId.toString()),
                eventCaptor.capture()
        );

        IssueEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getIssueId()).isEqualTo(issueId);
        assertThat(capturedEvent.getEventType()).isEqualTo(IssueEvent.EventType.STATUS_CHANGED);
        assertThat(capturedEvent.getStatus()).isEqualTo(newStatus);
        assertThat(capturedEvent.getPreviousStatus()).isEqualTo(previousStatus);
        assertThat(capturedEvent.getCategory()).isEqualTo(category);
        assertThat(capturedEvent.getPriority()).isEqualTo(priority);
    }

    @Test
    void sendStatusChange_toResolved_shouldSetCorrectEventType() {
        // Given
        UUID issueId = UUID.randomUUID();
        IssueStatus previousStatus = IssueStatus.IN_PROGRESS;
        IssueStatus newStatus = IssueStatus.RESOLVED;

        CompletableFuture<SendResult<String, IssueEvent>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendStatusChange(issueId, previousStatus, newStatus, IssueCategory.GRAFFITI, 3);

        // Then
        ArgumentCaptor<IssueEvent> eventCaptor = ArgumentCaptor.forClass(IssueEvent.class);
        verify(kafkaTemplate).send(any(), any(), eventCaptor.capture());

        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(IssueEvent.EventType.RESOLVED);
    }

    @Test
    void sendStatusChange_toClosed_shouldSetCorrectEventType() {
        // Given
        UUID issueId = UUID.randomUUID();
        IssueStatus previousStatus = IssueStatus.RESOLVED;
        IssueStatus newStatus = IssueStatus.CLOSED;

        CompletableFuture<SendResult<String, IssueEvent>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // When
        producer.sendStatusChange(issueId, previousStatus, newStatus, IssueCategory.TRASH, 4);

        // Then
        ArgumentCaptor<IssueEvent> eventCaptor = ArgumentCaptor.forClass(IssueEvent.class);
        verify(kafkaTemplate).send(any(), any(), eventCaptor.capture());

        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(IssueEvent.EventType.CLOSED);
    }
}
