package com.bmaas.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadResponse {
    private Long developerId;
    private String developerName;
    private Integer currentWorkload;
}
