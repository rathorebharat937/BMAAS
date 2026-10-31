package com.bmaas.dto.assignment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentResultDTO {

    private Long bugId;
    private Long projectId;
    private String projectName;
    private Long moduleId;
    private String moduleName;
    private String status; // "ASSIGNED" or "UNASSIGNED"
    private Long assignedDeveloperId;
    private String assignedDeveloperName;
    private String message;
    private Double winningScore;
    private LocalDateTime timestamp = LocalDateTime.now();
    private List<DeveloperScoreDTO> queueSnapshot = new ArrayList<>();

    public AssignmentResultDTO() {}

    public Long getBugId() { return bugId; }
    public void setBugId(Long bugId) { this.bugId = bugId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAssignedDeveloperId() { return assignedDeveloperId; }
    public void setAssignedDeveloperId(Long assignedDeveloperId) { this.assignedDeveloperId = assignedDeveloperId; }

    public String getAssignedDeveloperName() { return assignedDeveloperName; }
    public void setAssignedDeveloperName(String assignedDeveloperName) { this.assignedDeveloperName = assignedDeveloperName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Double getWinningScore() { return winningScore; }
    public void setWinningScore(Double winningScore) { this.winningScore = winningScore; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public List<DeveloperScoreDTO> getQueueSnapshot() { return queueSnapshot; }
    public void setQueueSnapshot(List<DeveloperScoreDTO> queueSnapshot) { this.queueSnapshot = queueSnapshot; }
}
