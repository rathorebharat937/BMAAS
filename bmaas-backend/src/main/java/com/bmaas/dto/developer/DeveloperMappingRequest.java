package com.bmaas.dto.developer;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /modules/{moduleId}/developers
 * roleOnModule is optional — e.g. "Lead Developer", "Contributor", "Reviewer"
 */
public class DeveloperMappingRequest {

    @NotNull(message = "Developer ID is required")
    private Long developerId;

    /** Optional role this developer plays on this specific module. */
    private String roleOnModule;

    public DeveloperMappingRequest() {}

    public DeveloperMappingRequest(Long developerId) {
        this.developerId = developerId;
    }

    public DeveloperMappingRequest(Long developerId, String roleOnModule) {
        this.developerId = developerId;
        this.roleOnModule = roleOnModule;
    }

    // Getters and Setters
    public Long getDeveloperId() { return developerId; }
    public void setDeveloperId(Long developerId) { this.developerId = developerId; }

    public String getRoleOnModule() { return roleOnModule; }
    public void setRoleOnModule(String roleOnModule) { this.roleOnModule = roleOnModule; }
}