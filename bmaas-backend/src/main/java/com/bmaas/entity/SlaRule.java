package com.bmaas.entity;

import com.bmaas.entity.Bug.BugPriority;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sla_rules")
@EntityListeners(AuditingEntityListener.class)
public class SlaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private BugPriority priority;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @Column(name = "warning_threshold_percent", nullable = false)
    private Integer warningThresholdPercent = 80;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SlaRule() {}

    public SlaRule(BugPriority priority, Integer durationHours, Integer warningThresholdPercent) {
        this.priority = priority;
        this.durationHours = durationHours;
        this.warningThresholdPercent = warningThresholdPercent != null ? warningThresholdPercent : 80;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BugPriority getPriority() { return priority; }
    public void setPriority(BugPriority priority) { this.priority = priority; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }

    public Integer getWarningThresholdPercent() { return warningThresholdPercent; }
    public void setWarningThresholdPercent(Integer warningThresholdPercent) { this.warningThresholdPercent = warningThresholdPercent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
