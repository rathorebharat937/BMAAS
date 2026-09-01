package com.bmaas.controller;

import com.bmaas.dto.ApiResponse;
import com.bmaas.dto.bug.BugRequest;
import com.bmaas.dto.bug.BugResponse;
import com.bmaas.dto.bug.BugStatusRequest;
import com.bmaas.entity.Bug.BugSeverity;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.User;
import com.bmaas.repository.UserRepository;
import com.bmaas.service.BugService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BugController {

    private final BugService bugService;
    private final UserRepository userRepository;
    private final com.bmaas.service.SlaService slaService;
    private final com.bmaas.service.DeveloperMetricsService developerMetricsService;

    public BugController(BugService bugService, UserRepository userRepository,
                         com.bmaas.service.SlaService slaService,
                         com.bmaas.service.DeveloperMetricsService developerMetricsService) {
        this.bugService = bugService;
        this.userRepository = userRepository;
        this.slaService = slaService;
        this.developerMetricsService = developerMetricsService;
    }

    @PostMapping("/projects/{projectId}/modules/{moduleId}/bugs")
    public ResponseEntity<ApiResponse> createBug(
            @PathVariable Long projectId,
            @PathVariable Long moduleId,
            @Valid @RequestBody BugRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reporterId = getCurrentUserId(userDetails);
        BugResponse bug = bugService.createBug(projectId, moduleId, request, reporterId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bug created successfully", bug));
    }

    @GetMapping("/projects/{projectId}/bugs")
    public ResponseEntity<ApiResponse> getBugsByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String slaStatus) {

        BugStatus bugStatus = null;
        BugSeverity bugSeverity = null;
        com.bmaas.entity.Bug.SlaStatus bugSlaStatus = null;

        if (status != null && !status.isBlank()) {
            bugStatus = BugStatus.valueOf(status.toUpperCase());
        }
        if (severity != null && !severity.isBlank()) {
            bugSeverity = BugSeverity.valueOf(severity.toUpperCase());
        }
        if (slaStatus != null && !slaStatus.isBlank()) {
            bugSlaStatus = com.bmaas.entity.Bug.SlaStatus.valueOf(slaStatus.toUpperCase());
        }

        List<BugResponse> bugs = bugService.getBugsByProject(projectId, moduleId, bugStatus, bugSeverity, bugSlaStatus);
        return ResponseEntity.ok(ApiResponse.success("Bugs retrieved successfully", bugs));
    }

    @GetMapping("/projects/{projectId}/bugs/sla-summary")
    public ResponseEntity<ApiResponse> getProjectSlaSummary(@PathVariable Long projectId) {
        com.bmaas.dto.sla.SlaSummaryResponse summary = slaService.getSlaSummaryForProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("SLA summary retrieved successfully", summary));
    }

    @GetMapping("/bugs/{bugId}")
    public ResponseEntity<ApiResponse> getBug(
            @PathVariable Long bugId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        BugResponse bug = bugService.getBugById(bugId, user.getId(), user.getRole());
        return ResponseEntity.ok(ApiResponse.success("Bug retrieved successfully", bug));
    }

    @GetMapping("/bugs/{bugId}/assignment-history")
    public ResponseEntity<ApiResponse> getBugAssignmentHistory(@PathVariable Long bugId) {
        List<com.bmaas.dto.metrics.AssignmentHistoryResponse> history = developerMetricsService.getAssignmentHistoryForBug(bugId);
        return ResponseEntity.ok(ApiResponse.success("Bug assignment history retrieved successfully", history));
    }

    @PatchMapping("/bugs/{bugId}/assign")
    public ResponseEntity<ApiResponse> assignBug(
            @PathVariable Long bugId,
            @Valid @RequestBody com.bmaas.dto.bug.BugAssignRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long assignedByUserId = userDetails != null ? getCurrentUserId(userDetails) : null;
        BugResponse bug = bugService.assignBug(bugId, request.getDeveloperId(), assignedByUserId);
        return ResponseEntity.ok(ApiResponse.success("Bug assigned successfully", bug));
    }

    @PatchMapping("/bugs/{bugId}/unassign")
    public ResponseEntity<ApiResponse> unassignBug(@PathVariable Long bugId) {
        BugResponse bug = bugService.unassignBug(bugId);
        return ResponseEntity.ok(ApiResponse.success("Bug unassigned successfully", bug));
    }

    @PutMapping("/bugs/{bugId}")
    public ResponseEntity<ApiResponse> updateBug(
            @PathVariable Long bugId,
            @Valid @RequestBody BugRequest request) {

        BugResponse bug = bugService.updateBug(bugId, request);
        return ResponseEntity.ok(ApiResponse.success("Bug updated successfully", bug));
    }

    @PatchMapping("/bugs/{bugId}/status")
    public ResponseEntity<ApiResponse> changeStatus(
            @PathVariable Long bugId,
            @Valid @RequestBody BugStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getCurrentUser(userDetails);
        BugStatus newStatus = BugStatus.valueOf(request.getStatus().toUpperCase());
        BugResponse bug = bugService.changeStatus(
                bugId, newStatus, request.getResolutionNotes(), user.getId(), user.getRole());
        return ResponseEntity.ok(ApiResponse.success("Bug status updated successfully", bug));
    }

    @DeleteMapping("/bugs/{bugId}")
    public ResponseEntity<ApiResponse> deleteBug(@PathVariable Long bugId) {
        bugService.deleteBug(bugId);
        return ResponseEntity.ok(ApiResponse.success("Bug deleted successfully"));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        return getCurrentUser(userDetails).getId();
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
