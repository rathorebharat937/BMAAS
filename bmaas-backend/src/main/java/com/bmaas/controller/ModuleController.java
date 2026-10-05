package com.bmaas.controller;

import com.bmaas.dto.ApiResponse;
import com.bmaas.dto.developer.DeveloperResponse;
import com.bmaas.dto.developer.DeveloperMappingRequest;
import com.bmaas.dto.module.ModuleMemberResponse;
import com.bmaas.dto.module.ModuleRequest;
import com.bmaas.dto.module.ModuleResponse;
import com.bmaas.service.DeveloperService;
import com.bmaas.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final DeveloperService developerService;

    // ── Project-based module endpoints ──────────────────────────────────────

    @GetMapping("/projects/{projectId}/modules")
    public ResponseEntity<ApiResponse> getModulesByProject(@PathVariable Long projectId) {
        List<ModuleResponse> modules = moduleService.getAllModulesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Modules retrieved successfully", modules));
    }

    @PostMapping("/projects/{projectId}/modules")
    public ResponseEntity<ApiResponse> createModule(@PathVariable Long projectId,
                                                     @Valid @RequestBody ModuleRequest request) {
        ModuleResponse module = moduleService.createModule(projectId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Module created successfully", module));
    }

    // ── Direct module endpoints ──────────────────────────────────────────────

    @GetMapping("/modules/{id}")
    public ResponseEntity<ApiResponse> getModule(@PathVariable Long id) {
        ModuleResponse module = moduleService.getModuleById(id);
        return ResponseEntity.ok(ApiResponse.success("Module retrieved successfully", module));
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<ApiResponse> updateModule(@PathVariable Long id,
                                                     @Valid @RequestBody ModuleRequest request) {
        ModuleResponse module = moduleService.updateModule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Module updated successfully", module));
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<ApiResponse> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.ok(ApiResponse.success("Module deleted successfully"));
    }

    // ── Developer-Module team membership endpoints ───────────────────────────
    // These manage TEAM membership (who is on the module's team).
    // They are SEPARATE from Phase 4's bug-assignment logic (BugController.assignBug).

    /**
     * Add a developer to a module's team, with optional role on this module.
     * Body: { "developerId": 1, "roleOnModule": "Lead Developer" }
     */
    @PostMapping("/modules/{moduleId}/developers")
    public ResponseEntity<ApiResponse> mapDeveloperToModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody DeveloperMappingRequest request) {
        developerService.mapDeveloperToModule(moduleId, request.getDeveloperId(), request.getRoleOnModule());
        return ResponseEntity.ok(ApiResponse.success("Developer added to module team successfully"));
    }

    @DeleteMapping("/modules/{moduleId}/developers/{developerId}")
    public ResponseEntity<ApiResponse> unmapDeveloperFromModule(@PathVariable Long moduleId,
                                                                 @PathVariable Long developerId) {
        developerService.unmapDeveloperFromModule(moduleId, developerId);
        return ResponseEntity.ok(ApiResponse.success("Developer removed from module team successfully"));
    }

    /**
     * Get all developers on a module's team (plain DeveloperResponse — backward compat).
     */
    @GetMapping("/modules/{moduleId}/developers")
    public ResponseEntity<ApiResponse> getDevelopersByModule(@PathVariable Long moduleId) {
        List<DeveloperResponse> developers = developerService.getDevelopersByModule(moduleId);
        return ResponseEntity.ok(ApiResponse.success("Developers retrieved successfully", developers));
    }

    /**
     * Get module team members with roleOnModule included (enriched response).
     * Frontend uses this for the Team panel in ProjectDetail.
     */
    @GetMapping("/modules/{moduleId}/members")
    public ResponseEntity<ApiResponse> getModuleMembers(@PathVariable Long moduleId) {
        List<ModuleMemberResponse> members = developerService.getModuleMembers(moduleId);
        return ResponseEntity.ok(ApiResponse.success("Module team members retrieved successfully", members));
    }
}