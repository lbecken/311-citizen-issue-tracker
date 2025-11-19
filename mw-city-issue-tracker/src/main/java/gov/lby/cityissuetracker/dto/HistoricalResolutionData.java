package gov.lby.cityissuetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalResolutionData {
    private Integer departmentId;
    private String category;
    private Double avgHours;
    private Double medianHours;
    private Long sampleCount;
}
