package com.bmaas.controller;

import com.bmaas.dto.ApiResponse;
import com.bmaas.dto.metrics.AssignmentHistoryResponse;
import com.bmaas.dto.metrics.DeveloperLeaderboardItemResponse;
import com.bmaas.dto.metrics.DeveloperMetricsResponse;
import com.bmaas.dto.metrics.WorkloadResponse;
import com.bmaas.entity.Developer;
import com.bmaas.entity.User;
import com.bmaas.entity.User.UserRole;
import com.bmaas.repository.DeveloperRepository;
import com.bmaas.repository.UserRepository;
import com.bmaas.service.DeveloperMetricsService;
import com.bmaas.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
public class DeveloperMetricsController {

    private final DeveloperMetricsService developerMetricsService;
    private final WorkloadService workloadService;
    private final DeveloperRepository developerRepository;
    private final UserRepository userRepository;

    @GetMapping("/{id}/metrics")
    public ResponseEntity<ApiResponse> getDeveloperMetrics(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        validateDeveloperOrPmAccess(id, userDetails);
        DeveloperMetricsResponse metrics = developerMetricsService.getMetricsForDeveloper(id);
        return ResponseEntity.ok(ApiResponse.success("Developer metrics retrieved successfully", metrics));
    }

    @GetMapping("/{id}/workload")
    public ResponseEntity<ApiResponse> getDeveloperWorkload(@PathVariable Long id) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new com.bmaas.exception.ResourceNotFoundException("Developer not found with id: " + id));
        int count = workloadService.getCurrentWorkload(id);
        WorkloadResponse res = WorkloadResponse.builder()
                .developerId(id)
                .developerName(developer.getName())
                .currentWorkload(count)
                .build();
        return ResponseEntity.ok(ApiResponse.success("Developer workload retrieved successfully", res));
    }

    @GetMapping("/{id}/assignment-history")
    public ResponseEntity<ApiResponse> getDeveloperAssignmentHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        validateDeveloperOrPmAccess(id, userDetails);
        List<AssignmentHistoryResponse> history = developerMetricsService.getAssignmentHistoryForDeveloper(id);
        return ResponseEntity.ok(ApiResponse.success("Developer assignment history retrieved successfully", history));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse> getLeaderboard(
            @RequestParam(required = false, defaultValue = "bugsResolved") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        List<DeveloperLeaderboardItemResponse> leaderboard = developerMetricsService.getLeaderboard(sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("Developer leaderboard retrieved successfully", leaderboard));
    }

    private void validateDeveloperOrPmAccess(Long developerId, UserDetails userDetails) {
        if (userDetails == null) return;
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return;

        if (user.getRole() == UserRole.DEVELOPER) {
            Developer dev = developerRepository.findByUserId(user.getId()).orElse(null);
            if (dev == null || !dev.getId().equals(developerId)) {
                throw new AccessDeniedException("You are not authorized to view another developer's private metrics");
            }
        }
    }
}
