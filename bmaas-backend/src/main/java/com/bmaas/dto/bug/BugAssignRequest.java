package com.bmaas.dto.bug;

import jakarta.validation.constraints.NotNull;

public class BugAssignRequest {

    @NotNull(message = "Developer ID is required")
    private Long developerId;

    public BugAssignRequest() {}

    public BugAssignRequest(Long developerId) {
        this.developerId = developerId;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }
}
