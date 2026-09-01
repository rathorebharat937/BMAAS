package com.bmaas.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "escalation_logs")
@EntityListeners(AuditingEntityListener.class)
public class EscalationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id", nullable = false)
    private Bug bug;

    @Column(name = "breached_at", nullable = false)
    private LocalDateTime breachedAt;

    @Column(name = "escalation_level", nullable = false, length = 50)
    private String escalationLevel = "PROJECT_MANAGER";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EscalationLog() {}

    public EscalationLog(Bug bug, LocalDateTime breachedAt, String escalationLevel) {
        this.bug = bug;
        this.breachedAt = breachedAt;
        this.escalationLevel = escalationLevel != null ? escalationLevel : "PROJECT_MANAGER";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bug getBug() { return bug; }
    public void setBug(Bug bug) { this.bug = bug; }

    public LocalDateTime getBreachedAt() { return breachedAt; }
    public void setBreachedAt(LocalDateTime breachedAt) { this.breachedAt = breachedAt; }

    public String getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(String escalationLevel) { this.escalationLevel = escalationLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
