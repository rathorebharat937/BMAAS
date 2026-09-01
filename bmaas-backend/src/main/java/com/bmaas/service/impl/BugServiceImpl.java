package com.bmaas.service.impl;

import com.bmaas.dto.bug.BugRequest;
import com.bmaas.dto.bug.BugResponse;
import com.bmaas.entity.Bug;
import com.bmaas.entity.Bug.BugPriority;
import com.bmaas.entity.Bug.BugSeverity;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.Bug.SlaStatus;
import com.bmaas.entity.Developer;
import com.bmaas.entity.Module;
import com.bmaas.entity.Project;
import com.bmaas.entity.User;
import com.bmaas.entity.User.UserRole;
import com.bmaas.exception.ResourceNotFoundException;
import com.bmaas.repository.BugRepository;
import com.bmaas.repository.DeveloperRepository;
import com.bmaas.repository.ModuleRepository;
import com.bmaas.repository.ProjectRepository;
import com.bmaas.repository.UserRepository;
import com.bmaas.service.BugService;
import com.bmaas.service.DeveloperMetricsService;
import com.bmaas.service.SlaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final DeveloperRepository developerRepository;
    private final SlaService slaService;
    private final DeveloperMetricsService developerMetricsService;

    // Full reconciled status transitions
    private static final Map<BugStatus, Set<BugStatus>> VALID_TRANSITIONS;

    static {
        Map<BugStatus, Set<BugStatus>> map = new EnumMap<>(BugStatus.class);
        map.put(BugStatus.OPEN, EnumSet.of(BugStatus.ASSIGNED, BugStatus.IN_PROGRESS, BugStatus.REJECTED));
        map.put(BugStatus.ASSIGNED, EnumSet.of(BugStatus.IN_PROGRESS, BugStatus.OPEN, BugStatus.REJECTED));
        map.put(BugStatus.IN_PROGRESS, EnumSet.of(BugStatus.RESOLVED, BugStatus.OPEN, BugStatus.ASSIGNED));
        map.put(BugStatus.RESOLVED, EnumSet.of(BugStatus.CLOSED, BugStatus.REOPENED));
        map.put(BugStatus.CLOSED, EnumSet.of(BugStatus.REOPENED));
        map.put(BugStatus.REOPENED, EnumSet.of(BugStatus.ASSIGNED, BugStatus.IN_PROGRESS, BugStatus.OPEN));
        map.put(BugStatus.REJECTED, EnumSet.of(BugStatus.REOPENED));
        VALID_TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public BugServiceImpl(BugRepository bugRepository, ProjectRepository projectRepository,
                          ModuleRepository moduleRepository, UserRepository userRepository,
                          DeveloperRepository developerRepository, SlaService slaService,
                          DeveloperMetricsService developerMetricsService) {
        this.bugRepository = bugRepository;
        this.projectRepository = projectRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.developerRepository = developerRepository;
        this.slaService = slaService;
        this.developerMetricsService = developerMetricsService;
    }

    @Override
    @Transactional
    public BugResponse createBug(Long projectId, Long moduleId, BugRequest request, Long reporterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        // Verify module belongs to the project
        if (!module.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Module " + moduleId + " does not belong to project " + projectId);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reporterId));

        Bug bug = new Bug();
        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setProject(project);
        bug.setModule(module);
        bug.setSeverity(BugSeverity.valueOf(request.getSeverity().toUpperCase()));
        BugPriority priority = BugPriority.valueOf(request.getPriority().toUpperCase());
        bug.setPriority(priority);
        bug.setStatus(BugStatus.OPEN);
        bug.setStepsToReproduce(request.getStepsToReproduce());
        bug.setExpectedResult(request.getExpectedResult());
        bug.setActualResult(request.getActualResult());
        bug.setEnvironment(request.getEnvironment());
        bug.setReporter(reporter);

        // Compute SLA deadline
        LocalDateTime now = LocalDateTime.now();
        bug.setSlaDeadline(slaService.calculateDeadline(now, priority));
        bug.setSlaStatus(SlaStatus.ON_TRACK);

        Bug savedBug = bugRepository.save(bug);
        return mapToResponse(savedBug);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BugResponse> getBugsByProject(Long projectId, Long moduleId, BugStatus status, BugSeverity severity, SlaStatus slaStatus) {
        // Verify project exists
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        return bugRepository.findByProjectWithFilters(projectId, moduleId, status, severity, slaStatus)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BugResponse> getBugsForDeveloper(Long userId, Long moduleId, BugStatus status, BugSeverity severity, SlaStatus slaStatus) {
        Developer developer = developerRepository.findByUserId(userId).orElse(null);
        if (developer == null) {
            return Collections.emptyList();
        }

        return bugRepository.findByAssignedDeveloperWithFilters(developer.getId(), moduleId, status, severity, slaStatus)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BugResponse getBugById(Long bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));
        return mapToResponse(bug);
    }

    @Override
    @Transactional(readOnly = true)
    public BugResponse getBugById(Long bugId, Long currentUserId, UserRole currentUserRole) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));

        if (currentUserRole == UserRole.DEVELOPER) {
            Developer dev = developerRepository.findByUserId(currentUserId).orElse(null);
            if (dev == null || bug.getAssignedDeveloper() == null || !bug.getAssignedDeveloper().getId().equals(dev.getId())) {
                throw new AccessDeniedException("You are not authorized to view this bug because it is not assigned to you.");
            }
        }

        return mapToResponse(bug);
    }

    @Override
    @Transactional
    public BugResponse updateBug(Long bugId, BugRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));

        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setSeverity(BugSeverity.valueOf(request.getSeverity().toUpperCase()));

        BugPriority newPriority = BugPriority.valueOf(request.getPriority().toUpperCase());
        if (bug.getPriority() != newPriority) {
            bug.setPriority(newPriority);
            LocalDateTime baseTime = bug.getCreatedAt() != null ? bug.getCreatedAt() : LocalDateTime.now();
            bug.setSlaDeadline(slaService.calculateDeadline(baseTime, newPriority));
        }

        bug.setStepsToReproduce(request.getStepsToReproduce());
        bug.setExpectedResult(request.getExpectedResult());
        bug.setActualResult(request.getActualResult());
        bug.setEnvironment(request.getEnvironment());

        Bug updatedBug = bugRepository.save(bug);
        return mapToResponse(updatedBug);
    }

    @Override
    @Transactional
    public BugResponse changeStatus(Long bugId, BugStatus newStatus) {
        return changeStatus(bugId, newStatus, null, null, null);
    }

    @Override
    @Transactional
    public BugResponse changeStatus(Long bugId, BugStatus newStatus, String resolutionNotes, Long currentUserId, UserRole currentUserRole) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));

        BugStatus currentStatus = bug.getStatus();
        Set<BugStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());

        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + currentStatus + " → " + newStatus +
                    ". Allowed transitions from " + currentStatus + ": " + allowed);
        }

        // Role authorization check
        if (currentUserRole == UserRole.DEVELOPER) {
            Developer dev = developerRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new AccessDeniedException("User is not linked to any developer profile"));

            if (bug.getAssignedDeveloper() == null || !bug.getAssignedDeveloper().getId().equals(dev.getId())) {
                throw new AccessDeniedException("You cannot change the status of a bug not assigned to you");
            }

            // Developer allowed transitions
            if (newStatus == BugStatus.RESOLVED) {
                if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
                    throw new IllegalArgumentException("Resolution notes are required when resolving a bug");
                }
                bug.setResolutionNotes(resolutionNotes.trim());
            } else if (newStatus != BugStatus.IN_PROGRESS && newStatus != BugStatus.ASSIGNED) {
                throw new AccessDeniedException("Developers can only transition bugs to IN_PROGRESS or RESOLVED");
            }
        } else if (currentUserRole == UserRole.TESTER) {
            // Tester allowed transitions
            if (newStatus == BugStatus.IN_PROGRESS || newStatus == BugStatus.ASSIGNED) {
                throw new AccessDeniedException("Testers cannot transition bugs to " + newStatus);
            }
        } else if (currentUserRole == UserRole.PROJECT_MANAGER) {
            if (newStatus == BugStatus.RESOLVED && resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
                bug.setResolutionNotes(resolutionNotes.trim());
            }
        }

        bug.setStatus(newStatus);

        // State-specific timestamps and developer metrics triggers
        if (newStatus == BugStatus.RESOLVED) {
            bug.setResolvedAt(LocalDateTime.now());
            bug.setLastResolvedBy(bug.getAssignedDeveloper());
        } else if (newStatus == BugStatus.CLOSED) {
            // Evaluate final SLA state
            slaService.evaluateStatus(bug);
            // Record closed metrics for the developer who resolved the bug
            developerMetricsService.recordBugClosed(bug);
        } else if (newStatus == BugStatus.REOPENED) {
            // Record reopen against the developer who resolved the bug
            developerMetricsService.recordBugReopened(bug);
        } else if (newStatus == BugStatus.REJECTED) {
            slaService.evaluateStatus(bug);
        }

        Bug updatedBug = bugRepository.save(bug);
        return mapToResponse(updatedBug);
    }

    @Override
    @Transactional
    public BugResponse assignBug(Long bugId, Long developerId) {
        return assignBug(bugId, developerId, null);
    }

    @Override
    @Transactional
    public BugResponse assignBug(Long bugId, Long developerId, Long assignedByUserId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));

        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + developerId));

        // Validate developer is mapped to the bug's module
        Module module = bug.getModule();
        boolean isMapped = module.getDevelopers().stream().anyMatch(d -> d.getId().equals(developerId));
        if (!isMapped) {
            throw new IllegalArgumentException(
                    "Developer " + developer.getName() + " (ID: " + developerId + 
                    ") is not mapped to module '" + module.getName() + "'. Please map the developer to the module first.");
        }

        User assignedByUser = assignedByUserId != null ? userRepository.findById(assignedByUserId).orElse(null) : null;

        bug.setAssignedDeveloper(developer);
        bug.setAssignedAt(LocalDateTime.now());
        bug.setStatus(BugStatus.ASSIGNED);

        // Record AssignmentHistory & Developer handled counter
        developerMetricsService.recordBugAssigned(developer, bug, assignedByUser);

        Bug updatedBug = bugRepository.save(bug);
        return mapToResponse(updatedBug);
    }

    @Override
    @Transactional
    public BugResponse unassignBug(Long bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));

        developerMetricsService.recordBugUnassigned(bug);

        bug.setAssignedDeveloper(null);
        bug.setStatus(BugStatus.OPEN);

        Bug updatedBug = bugRepository.save(bug);
        return mapToResponse(updatedBug);
    }

    @Override
    @Transactional
    public void deleteBug(Long bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with id: " + bugId));
        bugRepository.delete(bug);
    }

    private BugResponse mapToResponse(Bug bug) {
        BugResponse response = new BugResponse();
        response.setId(bug.getId());
        response.setTitle(bug.getTitle());
        response.setDescription(bug.getDescription());
        response.setProjectId(bug.getProject().getId());
        response.setProjectName(bug.getProject().getName());
        response.setModuleId(bug.getModule().getId());
        response.setModuleName(bug.getModule().getName());
        response.setSeverity(bug.getSeverity().name());
        response.setPriority(bug.getPriority().name());
        response.setStatus(bug.getStatus().name());
        response.setStepsToReproduce(bug.getStepsToReproduce());
        response.setExpectedResult(bug.getExpectedResult());
        response.setActualResult(bug.getActualResult());
        response.setEnvironment(bug.getEnvironment());
        response.setReporterId(bug.getReporter().getId());
        response.setReporterName(bug.getReporter().getFullName());

        if (bug.getAssignedDeveloper() != null) {
            response.setAssignedDeveloperId(bug.getAssignedDeveloper().getId());
            response.setAssignedDeveloperName(bug.getAssignedDeveloper().getName());
        }

        response.setResolutionNotes(bug.getResolutionNotes());

        // SLA evaluation
        SlaService.SlaEvaluationResult slaResult = slaService.evaluateStatus(bug);
        response.setSlaDeadline(bug.getSlaDeadline());
        response.setSlaStatus(slaResult.status().name());
        response.setSlaRemainingTime(slaResult.remainingTimeFormatted());
        response.setSlaBreachedAt(bug.getSlaBreachedAt());

        response.setCreatedAt(bug.getCreatedAt());
        response.setUpdatedAt(bug.getUpdatedAt());
        return response;
    }
}
