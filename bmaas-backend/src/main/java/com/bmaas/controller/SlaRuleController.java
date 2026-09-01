package com.bmaas.controller;

import com.bmaas.dto.ApiResponse;
import com.bmaas.dto.sla.SlaRuleRequest;
import com.bmaas.dto.sla.SlaRuleResponse;
import com.bmaas.service.SlaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla-rules")
public class SlaRuleController {

    private final SlaService slaService;

    public SlaRuleController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllRules() {
        List<SlaRuleResponse> rules = slaService.getAllRules();
        return ResponseEntity.ok(ApiResponse.success("SLA rules retrieved successfully", rules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getRule(@PathVariable Long id) {
        SlaRuleResponse rule = slaService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.success("SLA rule retrieved successfully", rule));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createRule(@Valid @RequestBody SlaRuleRequest request) {
        SlaRuleResponse rule = slaService.createRule(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("SLA rule created successfully", rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateRule(@PathVariable Long id, @Valid @RequestBody SlaRuleRequest request) {
        SlaRuleResponse rule = slaService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("SLA rule updated successfully", rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteRule(@PathVariable Long id) {
        slaService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("SLA rule deleted successfully"));
    }
}
