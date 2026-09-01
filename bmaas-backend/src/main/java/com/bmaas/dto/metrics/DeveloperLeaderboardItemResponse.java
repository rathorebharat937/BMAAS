package com.bmaas.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperLeaderboardItemResponse {
    private Long developerId;
    private String developerName;
    private String developerEmail;
    private String skillsTechStack;
    private Integer totalBugsHandled;
    private Integer bugsResolved;
    private Integer bugsReopened;
    private Double firstTimeFixRate;
    private Double averageResolutionTimeHours;
    private Double slaComplianceRate;
    private Integer currentWorkload;
    private Boolean hasHistory;
}
