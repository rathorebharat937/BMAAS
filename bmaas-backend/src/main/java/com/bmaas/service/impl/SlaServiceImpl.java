package com.bmaas.service.impl;

import com.bmaas.dto.sla.SlaRuleRequest;
import com.bmaas.dto.sla.SlaRuleResponse;
import com.bmaas.dto.sla.SlaSummaryResponse;
import com.bmaas.entity.Bug;
import com.bmaas.entity.Bug.BugPriority;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.Bug.SlaStatus;
import com.bmaas.entity.EscalationLog;
import com.bmaas.entity.SlaRule;
import com.bmaas.exception.ResourceAlreadyExistsException;
import com.bmaas.exception.ResourceNotFoundException;
import com.bmaas.repository.BugRepository;
import com.bmaas.repository.EscalationLogRepository;
import com.bmaas.repository.SlaRuleRepository;
import com.bmaas.service.SlaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlaServiceImpl implements SlaService {

    private final SlaRuleRepository slaRuleRepository;
    private final EscalationLogRepository escalationLogRepository;
    private final BugRepository bugRepository;

    public SlaServiceImpl(SlaRuleRepository slaRuleRepository,
                          EscalationLogRepository escalationLogRepository,
                          BugRepository bugRepository) {
        this.slaRuleRepository = slaRuleRepository;
        this.escalationLogRepository = escalationLogRepository;
        this.bugRepository = bugRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaRuleResponse> getAllRules() {
        return slaRuleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SlaRuleResponse getRuleById(Long id) {
        SlaRule rule = slaRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Rule not found with id: " + id));
        return mapToResponse(rule);
    }

    @Override
    @Transactional
    public SlaRuleResponse createRule(SlaRuleRequest request) {
        BugPriority priority = BugPriority.valueOf(request.getPriority().toUpperCase());
        if (slaRuleRepository.existsByPriority(priority)) {
            throw new ResourceAlreadyExistsException("SLA Rule for priority " + priority + " already exists");
        }

        SlaRule rule = new SlaRule(
                priority,
                request.getDurationHours(),
                request.getWarningThresholdPercent() != null ? request.getWarningThresholdPercent() : 80
        );

        SlaRule saved = slaRuleRepository.save(rule);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SlaRuleResponse updateRule(Long id, SlaRuleRequest request) {
        SlaRule rule = slaRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Rule not found with id: " + id));

        if (request.getDurationHours() != null) {
            rule.setDurationHours(request.getDurationHours());
        }
        if (request.getWarningThresholdPercent() != null) {
            rule.setWarningThresholdPercent(request.getWarningThresholdPercent());
        }

        SlaRule updated = slaRuleRepository.save(rule);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        SlaRule rule = slaRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Rule not found with id: " + id));
        slaRuleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public SlaRule getRuleForPriority(BugPriority priority) {
        return slaRuleRepository.findByPriority(priority)
                .orElseGet(() -> getDefaultFallbackRule(priority));
    }

    @Override
    public LocalDateTime calculateDeadline(LocalDateTime startTime, BugPriority priority) {
        SlaRule rule = getRuleForPriority(priority);
        return startTime.plusHours(rule.getDurationHours());
    }

    @Override
    @Transactional
    public SlaEvaluationResult evaluateStatus(Bug bug) {
        if (bug == null || bug.getSlaDeadline() == null) {
            return new SlaEvaluationResult(SlaStatus.ON_TRACK, "No SLA set");
        }

        // If bug is closed or rejected, freeze status as of completion
        if (bug.getStatus() == BugStatus.CLOSED || bug.getStatus() == BugStatus.REJECTED) {
            SlaStatus finalStatus = bug.getSlaStatus() != null ? bug.getSlaStatus() : SlaStatus.ON_TRACK;
            String label = bug.getStatus() == BugStatus.CLOSED ? "Closed" : "Rejected";
            return new SlaEvaluationResult(finalStatus, label);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = bug.getCreatedAt() != null ? bug.getCreatedAt() : now;
        LocalDateTime deadline = bug.getSlaDeadline();

        SlaRule rule = getRuleForPriority(bug.getPriority());
        int warningPercent = rule.getWarningThresholdPercent() != null ? rule.getWarningThresholdPercent() : 80;

        Duration totalDuration = Duration.between(createdAt, deadline);
        long totalSeconds = Math.max(1, totalDuration.getSeconds());
        LocalDateTime warningPoint = createdAt.plusSeconds((long) (totalSeconds * (warningPercent / 100.0)));

        SlaStatus status;
        String remainingTimeFormatted;

        if (now.isAfter(deadline)) {
            status = SlaStatus.BREACHED;
            Duration overdue = Duration.between(deadline, now);
            long h = overdue.toHours();
            long m = overdue.toMinutesPart();
            remainingTimeFormatted = h + "h " + m + "m overdue";

            // Record escalation once if not yet breached
            if (bug.getSlaBreachedAt() == null) {
                bug.setSlaBreachedAt(now);
                bug.setSlaStatus(SlaStatus.BREACHED);
                escalationLogRepository.save(new EscalationLog(bug, now, "PROJECT_MANAGER"));
            }
        } else if (now.isAfter(warningPoint)) {
            status = SlaStatus.AT_RISK;
            Duration remaining = Duration.between(now, deadline);
            long h = remaining.toHours();
            long m = remaining.toMinutesPart();
            remainingTimeFormatted = h + "h " + m + "m remaining";
        } else {
            status = SlaStatus.ON_TRACK;
            Duration remaining = Duration.between(now, deadline);
            long h = remaining.toHours();
            long m = remaining.toMinutesPart();
            remainingTimeFormatted = h + "h " + m + "m remaining";
        }

        bug.setSlaStatus(status);
        return new SlaEvaluationResult(status, remainingTimeFormatted);
    }

    @Override
    @Transactional
    public SlaSummaryResponse getSlaSummaryForProject(Long projectId) {
        List<Bug> bugs = bugRepository.findByProjectId(projectId);
        long onTrack = 0;
        long atRisk = 0;
        long breached = 0;

        for (Bug bug : bugs) {
            SlaEvaluationResult result = evaluateStatus(bug);
            switch (result.status()) {
                case ON_TRACK -> onTrack++;
                case AT_RISK -> atRisk++;
                case BREACHED -> breached++;
            }
        }

        return new SlaSummaryResponse(onTrack, atRisk, breached, bugs.size());
    }

    private SlaRule getDefaultFallbackRule(BugPriority priority) {
        return switch (priority) {
            case CRITICAL -> new SlaRule(BugPriority.CRITICAL, 4, 80);
            case HIGH -> new SlaRule(BugPriority.HIGH, 12, 80);
            case MEDIUM -> new SlaRule(BugPriority.MEDIUM, 24, 80);
            case LOW -> new SlaRule(BugPriority.LOW, 48, 80);
        };
    }

    private SlaRuleResponse mapToResponse(SlaRule rule) {
        return new SlaRuleResponse(
                rule.getId(),
                rule.getPriority().name(),
                rule.getDurationHours(),
                rule.getWarningThresholdPercent(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
