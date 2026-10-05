package com.bmaas.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Developer entity — enriched for Phase 6 scoring/queue readiness.
 * Phase 4's nullable userId link is preserved unchanged.
 * NOTE: scoring/eligibility/queue logic is NOT implemented here — these are data fields only.
 */
@Entity
@Table(name = "developers")
@EntityListeners(AuditingEntityListener.class)
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // ── Original field kept for backward compatibility ──────────────────────
    @Column(name = "skills_tech_stack", columnDefinition = "TEXT")
    private String skillsTechStack;

    // ── Phase 6 prep fields (structural only — no scoring logic here) ────────

    /** e.g. "Backend Developer", "Frontend Developer", "Full Stack Developer" */
    @Column(name = "job_title", length = 100)
    private String jobTitle;

    /** Comma-separated primary skills, e.g. "Java, Spring Boot, PostgreSQL" */
    @Column(name = "primary_skills", columnDefinition = "TEXT")
    private String primarySkills;

    /** Years of professional experience (decimal, e.g. 1.5, 3.0, 7.5) */
    @Column(name = "years_of_experience")
    private Double yearsOfExperience;

    /** Organizational department, e.g. "Engineering", "QA", "DevOps" */
    @Column(name = "department", length = 100)
    private String department;

    /** Unique company employee identifier, e.g. "EMP-001" */
    @Column(name = "employee_id", unique = true, length = 50)
    private String employeeId;

    /** Date the developer joined the organization */
    @Column(name = "date_joined")
    private LocalDate dateJoined;

    /**
     * Availability flag — false = on leave / temporarily unavailable.
     * Structural prep for Phase 6 queue eligibility (no auto-logic here).
     */
    @Column(name = "available", nullable = false)
    private boolean available = true;

    // ── Relationships ────────────────────────────────────────────────────────

    /**
     * Module memberships — navigated via ModuleDeveloper join entity
     * (replaces the old plain ManyToMany to allow roleOnModule per assignment).
     */
    @OneToMany(mappedBy = "developer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleDeveloper> moduleMemberships = new ArrayList<>();

    /** Phase 4: nullable link to a User account (DEVELOPER role). Do NOT remove. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSkillsTechStack() { return skillsTechStack; }
    public void setSkillsTechStack(String skillsTechStack) { this.skillsTechStack = skillsTechStack; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getPrimarySkills() { return primarySkills; }
    public void setPrimarySkills(String primarySkills) { this.primarySkills = primarySkills; }

    public Double getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Double yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDate dateJoined) { this.dateJoined = dateJoined; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public List<ModuleDeveloper> getModuleMemberships() { return moduleMemberships; }
    public void setModuleMemberships(List<ModuleDeveloper> moduleMemberships) { this.moduleMemberships = moduleMemberships; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ── Convenience helper for BugServiceImpl (replaces old getModules().contains()) ─
    public List<ModuleDeveloper> getModules() { return moduleMemberships; }
}