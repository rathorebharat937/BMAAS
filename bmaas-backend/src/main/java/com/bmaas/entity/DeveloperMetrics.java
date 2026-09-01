package com.bmaas.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "developer_metrics")
@EntityListeners(AuditingEntityListener.class)
public class DeveloperMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false, unique = true)
    private Developer developer;

    @Column(name = "total_bugs_handled", nullable = false)
    private Integer totalBugsHandled = 0;

    @Column(name = "bugs_resolved", nullable = false)
    private Integer bugsResolved = 0;

    @Column(name = "bugs_reopened", nullable = false)
    private Integer bugsReopened = 0;

    @Column(name = "total_resolution_time_minutes", nullable = false)
    private Long totalResolutionTimeMinutes = 0L;

    @Column(name = "sla_met_count", nullable = false)
    private Integer slaMetCount = 0;

    @Column(name = "sla_breached_count", nullable = false)
    private Integer slaBreachedCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DeveloperMetrics() {}

    public DeveloperMetrics(Developer developer) {
        this.developer = developer;
        this.totalBugsHandled = 0;
        this.bugsResolved = 0;
        this.bugsReopened = 0;
        this.totalResolutionTimeMinutes = 0L;
        this.slaMetCount = 0;
        this.slaBreachedCount = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Developer getDeveloper() { return developer; }
    public void setDeveloper(Developer developer) { this.developer = developer; }

    public Integer getTotalBugsHandled() { return totalBugsHandled; }
    public void setTotalBugsHandled(Integer totalBugsHandled) { this.totalBugsHandled = totalBugsHandled; }

    public Integer getBugsResolved() { return bugsResolved; }
    public void setBugsResolved(Integer bugsResolved) { this.bugsResolved = bugsResolved; }

    public Integer getBugsReopened() { return bugsReopened; }
    public void setBugsReopened(Integer bugsReopened) { this.bugsReopened = bugsReopened; }

    public Long getTotalResolutionTimeMinutes() { return totalResolutionTimeMinutes; }
    public void setTotalResolutionTimeMinutes(Long totalResolutionTimeMinutes) { this.totalResolutionTimeMinutes = totalResolutionTimeMinutes; }

    public Integer getSlaMetCount() { return slaMetCount; }
    public void setSlaMetCount(Integer slaMetCount) { this.slaMetCount = slaMetCount; }

    public Integer getSlaBreachedCount() { return slaBreachedCount; }
    public void setSlaBreachedCount(Integer slaBreachedCount) { this.slaBreachedCount = slaBreachedCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
