package com.bmaas.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperMetricsResponse {
    private Long developerId;
    private String developerName;
    private String developerEmail;
    private String skillsTechStack;
    private Long userId;
    private String userUsername;

    private Integer totalBugsHandled;
    private Integer bugsResolved;
    private Integer bugsReopened;

    // Derived metrics (null if no history / zero resolved)
    private Double firstTimeFixRate;
    private Double averageResolutionTimeHours;
    private Integer slaMetCount;
    private Integer slaBreachedCount;
    private Double slaComplianceRate;

    // Live calculated state
    private Integer currentWorkload;
    private Boolean hasHistory;
}
