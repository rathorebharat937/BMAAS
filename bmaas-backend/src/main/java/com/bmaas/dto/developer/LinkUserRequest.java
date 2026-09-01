package com.bmaas.dto.developer;

import jakarta.validation.constraints.NotNull;

public class LinkUserRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    public LinkUserRequest() {}

    public LinkUserRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
