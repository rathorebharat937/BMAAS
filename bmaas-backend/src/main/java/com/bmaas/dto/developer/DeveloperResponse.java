package com.bmaas.dto.developer;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Developer.
 * Enriched with Phase 6 prep fields.
 * Phase 4 userId/userUsername preserved.
 */
public class DeveloperResponse {

    private Long id;
    private String name;
    private String email;

    // Original field — preserved
    private String skillsTechStack;

    // Phase 6 prep fields
    private String jobTitle;
    private String primarySkills;
    private Double yearsOfExperience;
    private String department;
    private String employeeId;
    private LocalDate dateJoined;
    private boolean available;

    // Relational
    private Integer moduleCount;

    // Phase 4 user link
    private Long userId;
    private String userUsername;

    // Embedded metrics (Phase 5)
    private com.bmaas.dto.metrics.DeveloperMetricsResponse metrics;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeveloperResponse() {}

    // Getters and Setters
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

    public Integer getModuleCount() { return moduleCount; }
    public void setModuleCount(Integer moduleCount) { this.moduleCount = moduleCount; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserUsername() { return userUsername; }
    public void setUserUsername(String userUsername) { this.userUsername = userUsername; }

    public com.bmaas.dto.metrics.DeveloperMetricsResponse getMetrics() { return metrics; }
    public void setMetrics(com.bmaas.dto.metrics.DeveloperMetricsResponse metrics) { this.metrics = metrics; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}