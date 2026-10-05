package com.bmaas.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@EntityListeners(AuditingEntityListener.class)
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String technology;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModuleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModulePriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Team membership via join entity (replaces old plain @ManyToMany).
     * Allows storing roleOnModule per developer-module pairing.
     * Bug-assignment logic (Phase 4) is NOT changed — it uses BugServiceImpl
     * which checks module.getDevelopers() via the helper below.
     */
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleDeveloper> developerMemberships = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ModuleStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    public enum ModulePriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    public ModuleStatus getStatus() { return status; }
    public void setStatus(ModuleStatus status) { this.status = status; }

    public ModulePriority getPriority() { return priority; }
    public void setPriority(ModulePriority priority) { this.priority = priority; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public List<ModuleDeveloper> getDeveloperMemberships() { return developerMemberships; }
    public void setDeveloperMemberships(List<ModuleDeveloper> developerMemberships) {
        this.developerMemberships = developerMemberships;
    }

    /**
     * Convenience helper used by Phase 4 BugServiceImpl.assignBug()
     * which calls module.getDevelopers() to validate bug assignment eligibility.
     * Returns the actual Developer objects from the join entity list.
     */
    public List<Developer> getDevelopers() {
        List<Developer> developers = new ArrayList<>();
        for (ModuleDeveloper md : developerMemberships) {
            developers.add(md.getDeveloper());
        }
        return developers;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}