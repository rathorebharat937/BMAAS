package com.bmaas.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Join entity between Module and Developer.
 * Replaces the old plain @ManyToMany join table (developer_module_mapping)
 * to allow storing an optional roleOnModule per membership.
 *
 * Maps to table: module_developer_role
 * (old table developer_module_mapping is left in DB but no longer written to by JPA)
 *
 * Scope note: roleOnModule is about team membership only.
 * It has NO relation to Phase 4's bug-assignment logic.
 */
@Entity
@Table(
    name = "module_developer_role",
    uniqueConstraints = @UniqueConstraint(columnNames = {"module_id", "developer_id"})
)
@EntityListeners(AuditingEntityListener.class)
public class ModuleDeveloper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    private Developer developer;

    /**
     * Optional role this developer plays on this specific module.
     * Examples: "Lead Developer", "Contributor", "Reviewer", "DevOps Lead"
     * Different from Developer.jobTitle (which is their company-wide title).
     */
    @Column(name = "role_on_module", length = 100)
    private String roleOnModule;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    public ModuleDeveloper() {}

    public ModuleDeveloper(Module module, Developer developer, String roleOnModule) {
        this.module = module;
        this.developer = developer;
        this.roleOnModule = roleOnModule;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public Developer getDeveloper() { return developer; }
    public void setDeveloper(Developer developer) { this.developer = developer; }

    public String getRoleOnModule() { return roleOnModule; }
    public void setRoleOnModule(String roleOnModule) { this.roleOnModule = roleOnModule; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
}
