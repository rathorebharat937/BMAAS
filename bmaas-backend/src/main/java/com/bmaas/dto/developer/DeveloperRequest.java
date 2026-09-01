package com.bmaas.dto.developer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeveloperRequest {

    @NotBlank(message = "Developer name is required")
    @Size(max = 100, message = "Developer name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String skillsTechStack;

    public DeveloperRequest() {}

    public DeveloperRequest(String name, String email, String skillsTechStack) {
        this.name = name;
        this.email = email;
        this.skillsTechStack = skillsTechStack;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSkillsTechStack() { return skillsTechStack; }
    public void setSkillsTechStack(String skillsTechStack) { this.skillsTechStack = skillsTechStack; }
}