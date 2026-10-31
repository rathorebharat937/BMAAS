package com.bmaas.dto.assignment;

import java.time.LocalDate;

public class DeveloperScoreDTO {

    private Long developerId;
    private String developerName;
    private String email;
    private String jobTitle;
    private String roleOnModule;

    // Workload breakdown
    private int activeProjectsCount;
    private int activeModulesCount;
    private int activeBugsCount;
    private double rawWorkload;
    private double normalizedWorkloadScore; // 0.0 to 100.0 (higher = less workload)

    // Experience breakdown
    private LocalDate dateJoined;
    private double tenureYears;
    private double normalizedExperienceScore; // 0.0 to 100.0 (higher = more tenure)

    // Composite & Queue info
    private double compositeScore; // 0.0 to 100.0
    private int rank;
    private boolean selected;

    public DeveloperScoreDTO() {}

    // Getters and Setters
    public Long getDeveloperId() { return developerId; }
    public void setDeveloperId(Long developerId) { this.developerId = developerId; }

    public String getDeveloperName() { return developerName; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getRoleOnModule() { return roleOnModule; }
    public void setRoleOnModule(String roleOnModule) { this.roleOnModule = roleOnModule; }

    public int getActiveProjectsCount() { return activeProjectsCount; }
    public void setActiveProjectsCount(int activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }

    public int getActiveModulesCount() { return activeModulesCount; }
    public void setActiveModulesCount(int activeModulesCount) { this.activeModulesCount = activeModulesCount; }

    public int getActiveBugsCount() { return activeBugsCount; }
    public void setActiveBugsCount(int activeBugsCount) { this.activeBugsCount = activeBugsCount; }

    public double getRawWorkload() { return rawWorkload; }
    public void setRawWorkload(double rawWorkload) { this.rawWorkload = rawWorkload; }

    public double getNormalizedWorkloadScore() { return normalizedWorkloadScore; }
    public void setNormalizedWorkloadScore(double normalizedWorkloadScore) { this.normalizedWorkloadScore = normalizedWorkloadScore; }

    public LocalDate getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDate dateJoined) { this.dateJoined = dateJoined; }

    public double getTenureYears() { return tenureYears; }
    public void setTenureYears(double tenureYears) { this.tenureYears = tenureYears; }

    public double getNormalizedExperienceScore() { return normalizedExperienceScore; }
    public void setNormalizedExperienceScore(double normalizedExperienceScore) { this.normalizedExperienceScore = normalizedExperienceScore; }

    public double getCompositeScore() { return compositeScore; }
    public void setCompositeScore(double compositeScore) { this.compositeScore = compositeScore; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
