package com.bmaas.dto.module;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a developer team member on a module.
 * Includes both the developer's profile info and their role on this specific module.
 */
public class ModuleMemberResponse {

    private Long developerId;
    private String developerName;
    private String developerEmail;
    private String jobTitle;
    private String primarySkills;
    private Double yearsOfExperience;
    private String department;
    private String employeeId;
    private boolean available;

    // Role on this specific module
    private String roleOnModule;
    private LocalDateTime assignedAt;

    public ModuleMemberResponse() {}

    // Getters and Setters
    public Long getDeveloperId() { return developerId; }
    public void setDeveloperId(Long developerId) { this.developerId = developerId; }

    public String getDeveloperName() { return developerName; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }

    public String getDeveloperEmail() { return developerEmail; }
    public void setDeveloperEmail(String developerEmail) { this.developerEmail = developerEmail; }

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

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getRoleOnModule() { return roleOnModule; }
    public void setRoleOnModule(String roleOnModule) { this.roleOnModule = roleOnModule; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
