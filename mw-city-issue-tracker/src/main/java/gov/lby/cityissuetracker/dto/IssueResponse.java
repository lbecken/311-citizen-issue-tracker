package gov.lby.cityissuetracker.dto;

import gov.lby.cityissuetracker.entity.IssueCategory;
import gov.lby.cityissuetracker.entity.IssueStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder // Enables easy object creation for responses
public class IssueResponse {
    private UUID id;
    private String title;
    private String description;
    private IssueCategory category;
    private IssueStatus status;
    private Integer priority;
    private Double latitude;
    private Double longitude;
    private String address;
    private String reportedBy;
    private String reporterEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String departmentName; // Instead of full Department object
    private String workerId;
}