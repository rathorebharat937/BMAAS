package com.bmaas.service.impl;

import com.bmaas.dto.metrics.AssignmentHistoryResponse;
import com.bmaas.dto.metrics.DeveloperLeaderboardItemResponse;
import com.bmaas.dto.metrics.DeveloperMetricsResponse;
import com.bmaas.entity.AssignmentHistory;
import com.bmaas.entity.Bug;
import com.bmaas.entity.Developer;
import com.bmaas.entity.DeveloperMetrics;
import com.bmaas.entity.User;
import com.bmaas.exception.ResourceNotFoundException;
import com.bmaas.repository.AssignmentHistoryRepository;
import com.bmaas.repository.DeveloperMetricsRepository;
import com.bmaas.repository.DeveloperRepository;
import com.bmaas.service.DeveloperMetricsService;
import com.bmaas.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperMetricsServiceImpl implements DeveloperMetricsService {

    private final DeveloperMetricsRepository metricsRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final DeveloperRepository developerRepository;
    private final WorkloadService workloadService;

    @Override
    @Transactional
    public void recordBugAssigned(Developer developer, Bug bug, User assignedBy) {
        if (developer == null || bug == null) return;

        // Close out previous unassigned history for this bug if any
        assignmentHistoryRepository.findFirstByBugIdAndUnassignedAtIsNullOrderByAssignedAtDesc(bug.getId())
                .ifPresent(prev -> {
                    prev.setUnassignedAt(LocalDateTime.now());
                    assignmentHistoryRepository.save(prev);
                });

        // Insert new assignment history row
        AssignmentHistory history = new AssignmentHistory(bug, developer, assignedBy, LocalDateTime.now());
        assignmentHistoryRepository.save(history);

        // Update DeveloperMetrics counters
        DeveloperMetrics metrics = metricsRepository.findByDeveloperId(developer.getId())
                .orElseGet(() -> new DeveloperMetrics(developer));
        metrics.setTotalBugsHandled(metrics.getTotalBugsHandled() + 1);
        metricsRepository.save(metrics);
    }

    @Override
    @Transactional
    public void recordBugUnassigned(Bug bug) {
        if (bug == null) return;

        assignmentHistoryRepository.findFirstByBugIdAndUnassignedAtIsNullOrderByAssignedAtDesc(bug.getId())
                .ifPresent(active -> {
                    active.setUnassignedAt(LocalDateTime.now());
                    assignmentHistoryRepository.save(active);
                });
    }

    @Override
    @Transactional
    public void recordBugClosed(Bug bug) {
        if (bug == null) return;

        Developer dev = bug.getLastResolvedBy() != null ? bug.getLastResolvedBy() : bug.getAssignedDeveloper();
        if (dev == null) return;

        DeveloperMetrics metrics = metricsRepository.findByDeveloperId(dev.getId())
                .orElseGet(() -> new DeveloperMetrics(dev));

        metrics.setBugsResolved(metrics.getBugsResolved() + 1);

        // Calculate resolution time from assignedAt to resolvedAt (or now)
        LocalDateTime start = bug.getAssignedAt() != null ? bug.getAssignedAt() : bug.getCreatedAt();
        LocalDateTime end = bug.getResolvedAt() != null ? bug.getResolvedAt() : LocalDateTime.now();
        long minutes = Math.max(1, Duration.between(start, end).toMinutes());
        metrics.setTotalResolutionTimeMinutes(metrics.getTotalResolutionTimeMinutes() + minutes);

        // SLA Outcome at closure
        if (bug.getSlaStatus() == Bug.SlaStatus.BREACHED || bug.getSlaBreachedAt() != null) {
            metrics.setSlaBreachedCount(metrics.getSlaBreachedCount() + 1);
        } else {
            metrics.setSlaMetCount(metrics.getSlaMetCount() + 1);
        }

        metricsRepository.save(metrics);
    }

    @Override
    @Transactional
    public void recordBugReopened(Bug bug) {
        if (bug == null) return;

        Developer dev = bug.getLastResolvedBy() != null ? bug.getLastResolvedBy() : bug.getAssignedDeveloper();
        if (dev == null) return;

        DeveloperMetrics metrics = metricsRepository.findByDeveloperId(dev.getId())
                .orElseGet(() -> new DeveloperMetrics(dev));

        metrics.setBugsReopened(metrics.getBugsReopened() + 1);
        metricsRepository.save(metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperMetricsResponse getMetricsForDeveloper(Long developerId) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + developerId));

        DeveloperMetrics metrics = metricsRepository.findByDeveloperId(developerId)
                .orElse(new DeveloperMetrics(developer));

        return mapToResponse(developer, metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperLeaderboardItemResponse> getLeaderboard(String sortBy, String sortOrder) {
        List<Developer> developers = developerRepository.findAll();
        List<DeveloperMetrics> allMetrics = metricsRepository.findByDeveloperIn(developers);
        Map<Long, DeveloperMetrics> metricsMap = allMetrics.stream()
                .collect(Collectors.toMap(m -> m.getDeveloper().getId(), m -> m));

        List<DeveloperLeaderboardItemResponse> items = developers.stream().map(dev -> {
            DeveloperMetrics m = metricsMap.getOrDefault(dev.getId(), new DeveloperMetrics(dev));
            int resolved = m.getBugsResolved();
            int reopened = m.getBugsReopened();
            int workload = workloadService.getCurrentWorkload(dev.getId());

            Double fixRate = null;
            Double avgResHours = null;
            if (resolved > 0) {
                double rate = Math.max(0.0, Math.min(100.0, ((double) (resolved - reopened) / resolved) * 100.0));
                fixRate = Math.round(rate * 10.0) / 10.0;

                double hours = (double) m.getTotalResolutionTimeMinutes() / (60.0 * resolved);
                avgResHours = Math.round(hours * 10.0) / 10.0;
            }

            Double slaCompliance = null;
            int totalSla = m.getSlaMetCount() + m.getSlaBreachedCount();
            if (totalSla > 0) {
                double rate = ((double) m.getSlaMetCount() / totalSla) * 100.0;
                slaCompliance = Math.round(rate * 10.0) / 10.0;
            }

            boolean hasHistory = resolved > 0 || reopened > 0 || m.getTotalBugsHandled() > 0;

            return DeveloperLeaderboardItemResponse.builder()
                    .developerId(dev.getId())
                    .developerName(dev.getName())
                    .developerEmail(dev.getEmail())
                    .skillsTechStack(dev.getSkillsTechStack())
                    .totalBugsHandled(m.getTotalBugsHandled())
                    .bugsResolved(resolved)
                    .bugsReopened(reopened)
                    .firstTimeFixRate(fixRate)
                    .averageResolutionTimeHours(avgResHours)
                    .slaComplianceRate(slaCompliance)
                    .currentWorkload(workload)
                    .hasHistory(hasHistory)
                    .build();
        }).collect(Collectors.toList());

        // Sorting logic
        String sort = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy.trim().toLowerCase() : "bugsresolved";
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);

        Comparator<DeveloperLeaderboardItemResponse> comparator = switch (sort) {
            case "firsttimefixrate" -> Comparator.comparing(
                    DeveloperLeaderboardItemResponse::getFirstTimeFixRate,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "averageresolutiontimehours", "averageresolutiontime" -> Comparator.comparing(
                    DeveloperLeaderboardItemResponse::getAverageResolutionTimeHours,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "slacompliancerate", "slacompliance" -> Comparator.comparing(
                    DeveloperLeaderboardItemResponse::getSlaComplianceRate,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "currentworkload", "workload" -> Comparator.comparing(
                    DeveloperLeaderboardItemResponse::getCurrentWorkload,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "totalbugshandled" -> Comparator.comparing(
                    DeveloperLeaderboardItemResponse::getTotalBugsHandled,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            default -> Comparator.comparing(
                    DeveloperLeaderboardItemResponse::getBugsResolved,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }

        items.sort(comparator);
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentHistoryResponse> getAssignmentHistoryForBug(Long bugId) {
        return assignmentHistoryRepository.findByBugIdOrderByAssignedAtDesc(bugId).stream()
                .map(this::mapHistoryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentHistoryResponse> getAssignmentHistoryForDeveloper(Long developerId) {
        return assignmentHistoryRepository.findByDeveloperIdOrderByAssignedAtDesc(developerId).stream()
                .map(this::mapHistoryToResponse)
                .collect(Collectors.toList());
    }

    private DeveloperMetricsResponse mapToResponse(Developer developer, DeveloperMetrics metrics) {
        int resolved = metrics.getBugsResolved();
        int reopened = metrics.getBugsReopened();
        int workload = workloadService.getCurrentWorkload(developer.getId());

        Double fixRate = null;
        Double avgResHours = null;
        if (resolved > 0) {
            double rate = Math.max(0.0, Math.min(100.0, ((double) (resolved - reopened) / resolved) * 100.0));
            fixRate = Math.round(rate * 10.0) / 10.0;

            double hours = (double) metrics.getTotalResolutionTimeMinutes() / (60.0 * resolved);
            avgResHours = Math.round(hours * 10.0) / 10.0;
        }

        Double slaCompliance = null;
        int totalSla = metrics.getSlaMetCount() + metrics.getSlaBreachedCount();
        if (totalSla > 0) {
            double rate = ((double) metrics.getSlaMetCount() / totalSla) * 100.0;
            slaCompliance = Math.round(rate * 10.0) / 10.0;
        }

        boolean hasHistory = resolved > 0 || reopened > 0 || metrics.getTotalBugsHandled() > 0;

        return DeveloperMetricsResponse.builder()
                .developerId(developer.getId())
                .developerName(developer.getName())
                .developerEmail(developer.getEmail())
                .skillsTechStack(developer.getSkillsTechStack())
                .userId(developer.getUser() != null ? developer.getUser().getId() : null)
                .userUsername(developer.getUser() != null ? developer.getUser().getUsername() : null)
                .totalBugsHandled(metrics.getTotalBugsHandled())
                .bugsResolved(resolved)
                .bugsReopened(reopened)
                .firstTimeFixRate(fixRate)
                .averageResolutionTimeHours(avgResHours)
                .slaMetCount(metrics.getSlaMetCount())
                .slaBreachedCount(metrics.getSlaBreachedCount())
                .slaComplianceRate(slaCompliance)
                .currentWorkload(workload)
                .hasHistory(hasHistory)
                .build();
    }

    private AssignmentHistoryResponse mapHistoryToResponse(AssignmentHistory h) {
        return AssignmentHistoryResponse.builder()
                .id(h.getId())
                .bugId(h.getBug().getId())
                .bugTitle(h.getBug().getTitle())
                .developerId(h.getDeveloper().getId())
                .developerName(h.getDeveloper().getName())
                .developerEmail(h.getDeveloper().getEmail())
                .assignedById(h.getAssignedBy() != null ? h.getAssignedBy().getId() : null)
                .assignedByName(h.getAssignedBy() != null ? h.getAssignedBy().getFullName() : "System")
                .assignedAt(h.getAssignedAt())
                .unassignedAt(h.getUnassignedAt())
                .isCurrent(h.getUnassignedAt() == null)
                .build();
    }
}
