package com.bmaas.dto.bug;

import jakarta.validation.constraints.NotBlank;

public class BugStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String resolutionNotes;

    public BugStatusRequest() {}

    public BugStatusRequest(String status) {
        this.status = status;
    }

    public BugStatusRequest(String status, String resolutionNotes) {
        this.status = status;
        this.resolutionNotes = resolutionNotes;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
