package gov.lby.cityissuetracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull
    private String status; // Will convert to IssueStatus enum
    
    private String notes; // Optional
}