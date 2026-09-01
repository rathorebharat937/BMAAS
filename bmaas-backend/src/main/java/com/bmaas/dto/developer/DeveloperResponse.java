package com.bmaas.dto.developer;

import java.time.LocalDateTime;

public class DeveloperResponse {

    private Long id;
    private String name;
    private String email;
    private String skillsTechStack;
    private Integer moduleCount;
    private Long userId;
    private String userUsername;
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