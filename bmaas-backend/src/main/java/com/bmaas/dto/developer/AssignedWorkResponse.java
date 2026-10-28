package com.bmaas.dto.developer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a Project Module (Work Assignment) assigned to a developer by a PM.
 */
public class AssignedWorkResponse {

    private Long moduleId;
    private String moduleName;
    private String moduleDescription;
    private String moduleTechnology;
    private String moduleStatus;
    private String modulePriority;
    private String roleOnModule;
    private LocalDateTime assignedAt;

    // Project Info
    private Long projectId;
    private String projectName;
    private String projectDescription;
    private String projectStatus;
    private String projectTechnologyStack;
    private String projectManagerName;
    private String projectManagerEmail;

    // Work / Bug Counts
    private long totalModuleBugs;
    private long activeModuleBugs;
    private long myAssignedBugs;

    // Team members on this module
    private List<String> teamMemberNames;

    public AssignedWorkResponse() {}

    // Getters and Setters
    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getModuleDescription() { return moduleDescription; }
    public void setModuleDescription(String moduleDescription) { this.moduleDescription = moduleDescription; }

    public String getModuleTechnology() { return moduleTechnology; }
    public void setModuleTechnology(String moduleTechnology) { this.moduleTechnology = moduleTechnology; }

    public String getModuleStatus() { return moduleStatus; }
    public void setModuleStatus(String moduleStatus) { this.moduleStatus = moduleStatus; }

    public String getModulePriority() { return modulePriority; }
    public void setModulePriority(String modulePriority) { this.modulePriority = modulePriority; }

    public String getRoleOnModule() { return roleOnModule; }
    public void setRoleOnModule(String roleOnModule) { this.roleOnModule = roleOnModule; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }

    public String getProjectTechnologyStack() { return projectTechnologyStack; }
    public void setProjectTechnologyStack(String projectTechnologyStack) { this.projectTechnologyStack = projectTechnologyStack; }

    public String getProjectManagerName() { return projectManagerName; }
    public void setProjectManagerName(String projectManagerName) { this.projectManagerName = projectManagerName; }

    public String getProjectManagerEmail() { return projectManagerEmail; }
    public void setProjectManagerEmail(String projectManagerEmail) { this.projectManagerEmail = projectManagerEmail; }

    public long getTotalModuleBugs() { return totalModuleBugs; }
    public void setTotalModuleBugs(long totalModuleBugs) { this.totalModuleBugs = totalModuleBugs; }

    public long getActiveModuleBugs() { return activeModuleBugs; }
    public void setActiveModuleBugs(long activeModuleBugs) { this.activeModuleBugs = activeModuleBugs; }

    public long getMyAssignedBugs() { return myAssignedBugs; }
    public void setMyAssignedBugs(long myAssignedBugs) { this.myAssignedBugs = myAssignedBugs; }

    public List<String> getTeamMemberNames() { return teamMemberNames; }
    public void setTeamMemberNames(List<String> teamMemberNames) { this.teamMemberNames = teamMemberNames; }
}
