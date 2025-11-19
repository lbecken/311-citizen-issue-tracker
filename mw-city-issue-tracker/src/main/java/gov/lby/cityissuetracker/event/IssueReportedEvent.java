package gov.lby.cityissuetracker.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueReportedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID issueId;
    private Instant timestamp;
}
