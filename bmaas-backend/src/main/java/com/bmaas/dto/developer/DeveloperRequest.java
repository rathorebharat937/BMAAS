package com.bmaas.dto.developer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating/updating a Developer.
 * Phase 6 prep fields added (jobTitle, primarySkills, yearsOfExperience,
 * department, employeeId, dateJoined, available).
 * Existing fields (name, email, skillsTechStack) are unchanged.
 */
public class DeveloperRequest {

    @NotBlank(message = "Developer name is required")
    @Size(max = 100, message = "Developer name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // Original field — kept for backward compatibility
    private String skillsTechStack;

    // ── Phase 6 prep fields (optional on create/update) ─────────────────────
    private String jobTitle;
    private String primarySkills;
    private Double yearsOfExperience;
    private String department;
    private String employeeId;
    private String dateJoined;   // ISO date string "YYYY-MM-DD", parsed in service
    private Boolean available;   // null = keep existing; false = unavailable

    public DeveloperRequest() {}

    // Getters and Setters
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

    public String getDateJoined() { return dateJoined; }
    public void setDateJoined(String dateJoined) { this.dateJoined = dateJoined; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}