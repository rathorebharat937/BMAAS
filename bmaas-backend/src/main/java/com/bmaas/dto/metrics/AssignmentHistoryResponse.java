package com.bmaas.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentHistoryResponse {
    private Long id;
    private Long bugId;
    private String bugTitle;
    private Long developerId;
    private String developerName;
    private String developerEmail;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    private Boolean isCurrent;
}
