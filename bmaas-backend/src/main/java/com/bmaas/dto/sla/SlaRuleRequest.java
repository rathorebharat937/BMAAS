package com.bmaas.dto.sla;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SlaRuleRequest {

    private String priority;

    @NotNull(message = "Duration in hours is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;

    @Min(value = 1, message = "Warning threshold must be at least 1%")
    @Max(value = 99, message = "Warning threshold must be at most 99%")
    private Integer warningThresholdPercent = 80;

    public SlaRuleRequest() {}

    public SlaRuleRequest(String priority, Integer durationHours, Integer warningThresholdPercent) {
        this.priority = priority;
        this.durationHours = durationHours;
        this.warningThresholdPercent = warningThresholdPercent;
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public Integer getWarningThresholdPercent() { return warningThresholdPercent; }
    public void setWarningThresholdPercent(Integer warningThresholdPercent) { this.warningThresholdPercent = warningThresholdPercent; }
}
