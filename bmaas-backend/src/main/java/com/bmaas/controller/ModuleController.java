package com.bmaas.controller;

import com.bmaas.dto.ApiResponse;
import com.bmaas.dto.developer.DeveloperResponse;
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

    // Project-based module endpoints
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

    // Direct module endpoints
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

    // Developer-Module mapping endpoints
    @PostMapping("/modules/{moduleId}/developers")
    public ResponseEntity<ApiResponse> mapDeveloperToModule(@PathVariable Long moduleId,
                                                               @RequestBody com.bmaas.dto.developer.DeveloperMappingRequest request) {
        developerService.mapDeveloperToModule(moduleId, request.getDeveloperId());
        return ResponseEntity.ok(ApiResponse.success("Developer mapped to module successfully"));
    }

    @DeleteMapping("/modules/{moduleId}/developers/{developerId}")
    public ResponseEntity<ApiResponse> unmapDeveloperFromModule(@PathVariable Long moduleId,
                                                                   @PathVariable Long developerId) {
        developerService.unmapDeveloperFromModule(moduleId, developerId);
        return ResponseEntity.ok(ApiResponse.success("Developer unmapped from module successfully"));
    }

    @GetMapping("/modules/{moduleId}/developers")
    public ResponseEntity<ApiResponse> getDevelopersByModule(@PathVariable Long moduleId) {
        List<DeveloperResponse> developers = developerService.getDevelopersByModule(moduleId);
        return ResponseEntity.ok(ApiResponse.success("Developers retrieved successfully", developers));
    }
}