package com.bmaas.controller;

import com.bmaas.dto.ApiResponse;
import com.bmaas.dto.bug.BugResponse;
import com.bmaas.dto.developer.DeveloperRequest;
import com.bmaas.dto.developer.DeveloperResponse;
import com.bmaas.dto.developer.LinkUserRequest;
import com.bmaas.entity.Bug.BugSeverity;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.Bug.SlaStatus;
import com.bmaas.entity.User;
import com.bmaas.repository.UserRepository;
import com.bmaas.service.BugService;
import com.bmaas.service.DeveloperService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/developers")
public class DeveloperController {

    private final DeveloperService developerService;
    private final BugService bugService;
    private final UserRepository userRepository;

    public DeveloperController(DeveloperService developerService, BugService bugService, UserRepository userRepository) {
        this.developerService = developerService;
        this.bugService = bugService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllDevelopers() {
        List<DeveloperResponse> developers = developerService.getAllDevelopers();
        return ResponseEntity.ok(ApiResponse.success("Developers retrieved successfully", developers));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        DeveloperResponse developer = developerService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Developer profile retrieved successfully", developer));
    }

    @GetMapping("/me/bugs")
    public ResponseEntity<ApiResponse> getMyBugs(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String slaStatus,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        BugStatus bugStatus = null;
        BugSeverity bugSeverity = null;
        SlaStatus bugSlaStatus = null;

        if (status != null && !status.isBlank()) {
            bugStatus = BugStatus.valueOf(status.toUpperCase());
        }
        if (severity != null && !severity.isBlank()) {
            bugSeverity = BugSeverity.valueOf(severity.toUpperCase());
        }
        if (slaStatus != null && !slaStatus.isBlank()) {
            bugSlaStatus = SlaStatus.valueOf(slaStatus.toUpperCase());
        }

        List<BugResponse> bugs = bugService.getBugsForDeveloper(userId, moduleId, bugStatus, bugSeverity, bugSlaStatus);
        return ResponseEntity.ok(ApiResponse.success("Assigned bugs retrieved successfully", bugs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getDeveloper(@PathVariable Long id) {
        DeveloperResponse developer = developerService.getDeveloperById(id);
        return ResponseEntity.ok(ApiResponse.success("Developer retrieved successfully", developer));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createDeveloper(@Valid @RequestBody DeveloperRequest request) {
        DeveloperResponse developer = developerService.createDeveloper(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Developer created successfully", developer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateDeveloper(@PathVariable Long id,
                                                         @Valid @RequestBody DeveloperRequest request) {
        DeveloperResponse developer = developerService.updateDeveloper(id, request);
        return ResponseEntity.ok(ApiResponse.success("Developer updated successfully", developer));
    }

    @PutMapping("/{id}/link-user")
    public ResponseEntity<ApiResponse> linkUser(@PathVariable Long id,
                                                @Valid @RequestBody LinkUserRequest request) {
        DeveloperResponse developer = developerService.linkUser(id, request.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Developer linked to user successfully", developer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDeveloper(@PathVariable Long id) {
        developerService.deleteDeveloper(id);
        return ResponseEntity.ok(ApiResponse.success("Developer deleted successfully"));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}