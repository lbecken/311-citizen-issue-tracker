package gov.lby.cityissuetracker.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Spring application event published when an issue is created.
 * This is used internally to trigger RabbitMQ message publishing after transaction commit.
 */
public class IssueCreatedApplicationEvent extends ApplicationEvent {

    private final UUID issueId;

    public IssueCreatedApplicationEvent(Object source, UUID issueId) {
        super(source);
        this.issueId = issueId;
    }

    public UUID getIssueId() {
        return issueId;
    }
}
