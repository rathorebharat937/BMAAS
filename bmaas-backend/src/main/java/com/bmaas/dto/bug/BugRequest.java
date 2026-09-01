package com.bmaas.dto.bug;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BugRequest {

    @NotBlank(message = "Bug title is required")
    @Size(max = 300, message = "Bug title must not exceed 300 characters")
    private String title;

    private String description;

    @NotBlank(message = "Severity is required")
    private String severity;

    @NotBlank(message = "Priority is required")
    private String priority;

    private String stepsToReproduce;

    private String expectedResult;

    private String actualResult;

    @Size(max = 500, message = "Environment must not exceed 500 characters")
    private String environment;

    public BugRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStepsToReproduce() { return stepsToReproduce; }
    public void setStepsToReproduce(String stepsToReproduce) { this.stepsToReproduce = stepsToReproduce; }

    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

    public String getActualResult() { return actualResult; }
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
}
