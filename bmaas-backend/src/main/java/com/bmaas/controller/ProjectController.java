package com.bmaas.controller;

import com.bmaas.dto.ApiResponse;
import com.bmaas.dto.project.ProjectRequest;
import com.bmaas.dto.project.ProjectResponse;
import com.bmaas.entity.User;
import com.bmaas.repository.UserRepository;
import com.bmaas.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProjects(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<ProjectResponse> projects;
        if (user.getRole() == User.UserRole.PROJECT_MANAGER) {
            projects = projectService.getAllProjectsByManager(user.getId());
        } else {
            projects = projectService.getAllProjects();
        }
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProject(@PathVariable Long id, 
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        ProjectResponse project;
        if (user.getRole() == User.UserRole.PROJECT_MANAGER) {
            project = projectService.getProjectById(id, user.getId());
        } else {
            project = projectService.getProjectById(id);
        }
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", project));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createProject(@Valid @RequestBody ProjectRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        ProjectResponse project = projectService.createProject(request, userId);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Project created successfully", project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProject(@PathVariable Long id,
                                                       @Valid @RequestBody ProjectRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        ProjectResponse project = projectService.updateProject(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProject(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        projectService.deleteProject(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        return getCurrentUser(userDetails).getId();
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}