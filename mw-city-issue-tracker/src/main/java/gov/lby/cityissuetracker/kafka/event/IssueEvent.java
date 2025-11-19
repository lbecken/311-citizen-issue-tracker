package gov.lby.cityissuetracker.kafka.event;

import gov.lby.cityissuetracker.entity.IssueStatus;
import gov.lby.cityissuetracker.entity.IssueCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueEvent {

    private UUID issueId;
    private EventType eventType;
    private IssueStatus status;
    private IssueStatus previousStatus;
    private IssueCategory category;
    private Integer priority;
    private Instant timestamp;

    public enum EventType {
        CREATED,
        STATUS_CHANGED,
        PRIORITY_CHANGED,
        ASSIGNED,
        RESOLVED,
        CLOSED
    }

    public static IssueEvent created(UUID issueId, IssueStatus status, IssueCategory category, Integer priority) {
        return IssueEvent.builder()
                .issueId(issueId)
                .eventType(EventType.CREATED)
                .status(status)
                .category(category)
                .priority(priority)
                .timestamp(Instant.now())
                .build();
    }

    public static IssueEvent statusChanged(UUID issueId, IssueStatus previousStatus, IssueStatus newStatus,
                                           IssueCategory category, Integer priority) {
        EventType eventType = switch (newStatus) {
            case RESOLVED -> EventType.RESOLVED;
            case CLOSED -> EventType.CLOSED;
            case ASSIGNED -> EventType.ASSIGNED;
            default -> EventType.STATUS_CHANGED;
        };

        return IssueEvent.builder()
                .issueId(issueId)
                .eventType(eventType)
                .status(newStatus)
                .previousStatus(previousStatus)
                .category(category)
                .priority(priority)
                .timestamp(Instant.now())
                .build();
    }
}
