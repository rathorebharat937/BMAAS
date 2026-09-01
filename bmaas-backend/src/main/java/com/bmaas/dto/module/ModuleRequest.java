package com.bmaas.dto.module;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ModuleRequest {

    @NotBlank(message = "Module name is required")
    @Size(max = 200, message = "Module name must not exceed 200 characters")
    private String name;

    private String description;

    private String technology;

    @NotBlank(message = "Module status is required")
    private String status;

    @NotBlank(message = "Module priority is required")
    private String priority;

    public ModuleRequest() {}

    public ModuleRequest(String name, String description, String technology, String status, String priority) {
        this.name = name;
        this.description = description;
        this.technology = technology;
        this.status = status;
        this.priority = priority;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}