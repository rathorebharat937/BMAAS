package com.bmaas.dto.developer;

import jakarta.validation.constraints.NotNull;

public class DeveloperMappingRequest {

    @NotNull(message = "Developer ID is required")
    private Long developerId;

    public DeveloperMappingRequest() {}

    public DeveloperMappingRequest(Long developerId) {
        this.developerId = developerId;
    }

    // Getters and Setters
    public Long getDeveloperId() { return developerId; }
    public void setDeveloperId(Long developerId) { this.developerId = developerId; }
}