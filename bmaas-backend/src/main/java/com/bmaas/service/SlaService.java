package com.bmaas.service;

import com.bmaas.dto.sla.SlaRuleRequest;
import com.bmaas.dto.sla.SlaRuleResponse;
import com.bmaas.dto.sla.SlaSummaryResponse;
import com.bmaas.entity.Bug;
import com.bmaas.entity.Bug.BugPriority;
import com.bmaas.entity.Bug.SlaStatus;
import com.bmaas.entity.SlaRule;

import java.time.LocalDateTime;
import java.util.List;

public interface SlaService {

    List<SlaRuleResponse> getAllRules();

    SlaRuleResponse getRuleById(Long id);

    SlaRuleResponse createRule(SlaRuleRequest request);

    SlaRuleResponse updateRule(Long id, SlaRuleRequest request);

    void deleteRule(Long id);

    SlaRule getRuleForPriority(BugPriority priority);

    LocalDateTime calculateDeadline(LocalDateTime startTime, BugPriority priority);

    SlaEvaluationResult evaluateStatus(Bug bug);

    SlaSummaryResponse getSlaSummaryForProject(Long projectId);

    record SlaEvaluationResult(SlaStatus status, String remainingTimeFormatted) {}
}
