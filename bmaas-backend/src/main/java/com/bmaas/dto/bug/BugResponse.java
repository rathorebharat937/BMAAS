package com.bmaas.dto.bug;

import java.time.LocalDateTime;

public class BugResponse {

    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private String projectName;
    private Long moduleId;
    private String moduleName;
    private String severity;
    private String priority;
    private String status;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String environment;
    private Long reporterId;
    private String reporterName;
    private Long assignedDeveloperId;
    private String assignedDeveloperName;
    private String resolutionNotes;
    private LocalDateTime slaDeadline;
    private String slaStatus;
    private String slaRemainingTime;
    private LocalDateTime slaBreachedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BugResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStepsToReproduce() { return stepsToReproduce; }
    public void setStepsToReproduce(String stepsToReproduce) { this.stepsToReproduce = stepsToReproduce; }

    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

    public String getActualResult() { return actualResult; }
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public Long getAssignedDeveloperId() { return assignedDeveloperId; }
    public void setAssignedDeveloperId(Long assignedDeveloperId) { this.assignedDeveloperId = assignedDeveloperId; }

    public String getAssignedDeveloperName() { return assignedDeveloperName; }
    public void setAssignedDeveloperName(String assignedDeveloperName) { this.assignedDeveloperName = assignedDeveloperName; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public String getSlaStatus() { return slaStatus; }
    public void setSlaStatus(String slaStatus) { this.slaStatus = slaStatus; }

    public String getSlaRemainingTime() { return slaRemainingTime; }
    public void setSlaRemainingTime(String slaRemainingTime) { this.slaRemainingTime = slaRemainingTime; }

    public LocalDateTime getSlaBreachedAt() { return slaBreachedAt; }
    public void setSlaBreachedAt(LocalDateTime slaBreachedAt) { this.slaBreachedAt = slaBreachedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
