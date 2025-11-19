package gov.lby.cityissuetracker.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {

    private long totalIssues;
    private long openIssues;
    private long resolvedIssues;
    private long closedIssues;
    private Map<String, Long> issuesByStatus;
    private Map<String, Long> issuesByCategory;
    private Map<String, Long> issuesByPriority;
    private long issuesCreatedLast5Minutes;
    private long issuesResolvedLast5Minutes;
    private Instant timestamp;
}
