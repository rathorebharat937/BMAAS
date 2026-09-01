package com.bmaas.dto.sla;

import java.time.LocalDateTime;

public class SlaRuleResponse {

    private Long id;
    private String priority;
    private Integer durationHours;
    private Integer warningThresholdPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SlaRuleResponse() {}

    public SlaRuleResponse(Long id, String priority, Integer durationHours, Integer warningThresholdPercent, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.priority = priority;
        this.durationHours = durationHours;
        this.warningThresholdPercent = warningThresholdPercent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public Integer getWarningThresholdPercent() { return warningThresholdPercent; }
    public void setWarningThresholdPercent(Integer warningThresholdPercent) { this.warningThresholdPercent = warningThresholdPercent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
