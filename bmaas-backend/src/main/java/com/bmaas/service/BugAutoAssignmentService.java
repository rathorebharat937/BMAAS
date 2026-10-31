package com.bmaas.service;

import com.bmaas.dto.assignment.AssignmentResultDTO;
import com.bmaas.dto.assignment.DeveloperScoreDTO;
import com.bmaas.entity.Bug;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.Developer;
import com.bmaas.entity.Module;
import com.bmaas.entity.ModuleDeveloper;
import com.bmaas.entity.User;
import com.bmaas.repository.BugRepository;
import com.bmaas.repository.DeveloperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BugAutoAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(BugAutoAssignmentService.class);

    private final DeveloperEligibilityService eligibilityService;
    private final DynamicPriorityQueueService queueService;
    private final BugRepository bugRepository;
    private final DeveloperRepository developerRepository;
    private final DeveloperMetricsService developerMetricsService;

    public BugAutoAssignmentService(DeveloperEligibilityService eligibilityService,
                                    DynamicPriorityQueueService queueService,
                                    BugRepository bugRepository,
                                    DeveloperRepository developerRepository,
                                    DeveloperMetricsService developerMetricsService) {
        this.eligibilityService = eligibilityService;
        this.queueService = queueService;
        this.bugRepository = bugRepository;
        this.developerRepository = developerRepository;
        this.developerMetricsService = developerMetricsService;
    }

    /**
     * Executes the automatic bug assignment pipeline.
     */
    @Transactional
    public AssignmentResultDTO autoAssignBug(Bug bug, User reporter) {
        Module module = bug.getModule();
        Long moduleId = module.getId();

        AssignmentResultDTO result = new AssignmentResultDTO();
        result.setBugId(bug.getId());
        result.setProjectId(bug.getProject().getId());
        result.setProjectName(bug.getProject().getName());
        result.setModuleId(moduleId);
        result.setModuleName(module.getName());
        result.setTimestamp(LocalDateTime.now());

        // 1. Module-based developer filtering
        List<ModuleDeveloper> eligibleMembers = eligibilityService.getEligibleModuleDevelopers(moduleId);

        if (eligibleMembers.isEmpty()) {
            log.warn("No eligible developers found for module '{}' (ID: {}). Bug #{} will remain unassigned.",
                    module.getName(), moduleId, bug.getId());

            bug.setAssignedDeveloper(null);
            bug.setStatus(BugStatus.OPEN);
            bugRepository.save(bug);

            result.setStatus("UNASSIGNED");
            result.setMessage("No eligible developers are currently mapped or available for module '" +
                    module.getName() + "'. Bug has been saved as UNASSIGNED for manual assignment by Project Manager.");
            return result;
        }

        // 2. Dynamic Priority Queue scoring & ranking
        List<DeveloperScoreDTO> rankedCandidates = queueService.buildAndRankCandidates(eligibleMembers);
        result.setQueueSnapshot(rankedCandidates);

        DeveloperScoreDTO winner = rankedCandidates.get(0);
        Developer developer = developerRepository.findById(winner.getDeveloperId()).orElse(null);

        if (developer == null) {
            log.error("Top-ranked developer ID {} not found in database", winner.getDeveloperId());
            result.setStatus("UNASSIGNED");
            result.setMessage("Top selected developer was not found in database. Bug saved as UNASSIGNED.");
            return result;
        }

        // 3. Assign bug to the top-ranked developer
        bug.setAssignedDeveloper(developer);
        bug.setAssignedAt(LocalDateTime.now());
        bug.setStatus(BugStatus.ASSIGNED);

        developerMetricsService.recordBugAssigned(developer, bug, reporter);
        bugRepository.save(bug);

        result.setStatus("ASSIGNED");
        result.setAssignedDeveloperId(developer.getId());
        result.setAssignedDeveloperName(developer.getName());
        result.setWinningScore(winner.getCompositeScore());
        result.setMessage("Bug automatically assigned to " + developer.getName() +
                " based on Dynamic Priority Queue (Score: " + winner.getCompositeScore() + "/100).");

        log.info("Bug #{} automatically assigned to developer '{}' (score: {}) for module '{}'",
                bug.getId(), developer.getName(), winner.getCompositeScore(), module.getName());

        return result;
    }

    /**
     * Preview the current priority queue for a module without assigning any bug.
     */
    @Transactional(readOnly = true)
    public List<DeveloperScoreDTO> previewQueueForModule(Long moduleId) {
        List<ModuleDeveloper> eligibleMembers = eligibilityService.getEligibleModuleDevelopers(moduleId);
        return queueService.buildAndRankCandidates(eligibleMembers);
    }
}
