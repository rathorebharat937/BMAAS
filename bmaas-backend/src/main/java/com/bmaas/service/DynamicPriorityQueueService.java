package com.bmaas.service;

import com.bmaas.config.QueueConfigProperties;
import com.bmaas.dto.assignment.DeveloperScoreDTO;
import com.bmaas.entity.Developer;
import com.bmaas.entity.ModuleDeveloper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DynamicPriorityQueueService {

    private final DeveloperWorkloadScoringService workloadScoringService;
    private final DeveloperExperienceScoringService experienceScoringService;
    private final QueueConfigProperties queueConfig;

    public DynamicPriorityQueueService(DeveloperWorkloadScoringService workloadScoringService,
                                       DeveloperExperienceScoringService experienceScoringService,
                                       QueueConfigProperties queueConfig) {
        this.workloadScoringService = workloadScoringService;
        this.experienceScoringService = experienceScoringService;
        this.queueConfig = queueConfig;
    }

    /**
     * Comparator for PriorityQueue:
     * 1. Higher compositeScore comes first (descending)
     * 2. Lower rawWorkload comes first (ascending tie-breaker)
     * 3. Higher tenureYears comes first (descending tie-breaker)
     * 4. Lower developerId comes first (deterministic tie-breaker)
     */
    private static final Comparator<DeveloperScoreDTO> CANDIDATE_COMPARATOR = (a, b) -> {
        int cmp = Double.compare(b.getCompositeScore(), a.getCompositeScore());
        if (cmp != 0) return cmp;

        cmp = Double.compare(a.getRawWorkload(), b.getRawWorkload());
        if (cmp != 0) return cmp;

        cmp = Double.compare(b.getTenureYears(), a.getTenureYears());
        if (cmp != 0) return cmp;

        return Long.compare(a.getDeveloperId(), b.getDeveloperId());
    };

    /**
     * Build the dynamic priority queue and return the ordered candidate list.
     */
    public List<DeveloperScoreDTO> buildAndRankCandidates(List<ModuleDeveloper> eligibleMembers) {
        if (eligibleMembers == null || eligibleMembers.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. First pass: compute raw workload and experience for all candidates
        List<CandidateIntermediate> intermediates = new ArrayList<>(eligibleMembers.size());
        double minRawWorkload = Double.MAX_VALUE;
        double maxRawWorkload = Double.MIN_VALUE;

        for (ModuleDeveloper md : eligibleMembers) {
            Developer dev = md.getDeveloper();
            DeveloperWorkloadScoringService.WorkloadMetrics wMetrics =
                    workloadScoringService.calculateRawWorkload(dev.getId());
            DeveloperExperienceScoringService.ExperienceMetrics expMetrics =
                    experienceScoringService.calculateExperience(dev);

            if (wMetrics.rawWorkload < minRawWorkload) {
                minRawWorkload = wMetrics.rawWorkload;
            }
            if (wMetrics.rawWorkload > maxRawWorkload) {
                maxRawWorkload = wMetrics.rawWorkload;
            }

            intermediates.add(new CandidateIntermediate(dev, md.getRoleOnModule(), wMetrics, expMetrics));
        }

        // 2. Second pass: normalize scores and build PriorityQueue
        PriorityQueue<DeveloperScoreDTO> maxHeap = new PriorityQueue<>(
                Math.max(1, intermediates.size()),
                CANDIDATE_COMPARATOR
        );

        double wWeight = queueConfig.getWeights().getWorkload();
        double expWeight = queueConfig.getWeights().getExperience();

        // Safety normalize weights if they do not sum to 1.0
        double totalWeight = wWeight + expWeight;
        if (totalWeight <= 0) {
            wWeight = 0.60;
            expWeight = 0.40;
            totalWeight = 1.0;
        }
        wWeight = wWeight / totalWeight;
        expWeight = expWeight / totalWeight;

        for (CandidateIntermediate item : intermediates) {
            double normalizedWorkload = workloadScoringService.normalizeWorkloadScore(
                    item.wMetrics.rawWorkload, minRawWorkload, maxRawWorkload);
            double normalizedExp = item.expMetrics.normalizedScore;

            double composite = (wWeight * normalizedWorkload) + (expWeight * normalizedExp);
            // Round to 2 decimal places
            composite = Math.round(composite * 100.0) / 100.0;

            DeveloperScoreDTO dto = new DeveloperScoreDTO();
            dto.setDeveloperId(item.dev.getId());
            dto.setDeveloperName(item.dev.getName());
            dto.setEmail(item.dev.getEmail());
            dto.setJobTitle(item.dev.getJobTitle());
            dto.setRoleOnModule(item.roleOnModule);

            dto.setActiveProjectsCount(item.wMetrics.activeProjects);
            dto.setActiveModulesCount(item.wMetrics.activeModules);
            dto.setActiveBugsCount(item.wMetrics.activeBugs);
            dto.setRawWorkload(Math.round(item.wMetrics.rawWorkload * 100.0) / 100.0);
            dto.setNormalizedWorkloadScore(Math.round(normalizedWorkload * 100.0) / 100.0);

            dto.setDateJoined(item.expMetrics.dateJoined);
            dto.setTenureYears(Math.round(item.expMetrics.tenureYears * 10.0) / 10.0);
            dto.setNormalizedExperienceScore(Math.round(normalizedExp * 100.0) / 100.0);

            dto.setCompositeScore(composite);

            maxHeap.add(dto);
        }

        // 3. Extract in prioritized order
        List<DeveloperScoreDTO> ranked = new ArrayList<>(maxHeap.size());
        int rank = 1;
        while (!maxHeap.isEmpty()) {
            DeveloperScoreDTO top = maxHeap.poll();
            top.setRank(rank);
            if (rank == 1) {
                top.setSelected(true);
            }
            ranked.add(top);
            rank++;
        }

        return ranked;
    }

    private static class CandidateIntermediate {
        final Developer dev;
        final String roleOnModule;
        final DeveloperWorkloadScoringService.WorkloadMetrics wMetrics;
        final DeveloperExperienceScoringService.ExperienceMetrics expMetrics;

        CandidateIntermediate(Developer dev, String roleOnModule,
                              DeveloperWorkloadScoringService.WorkloadMetrics wMetrics,
                              DeveloperExperienceScoringService.ExperienceMetrics expMetrics) {
            this.dev = dev;
            this.roleOnModule = roleOnModule;
            this.wMetrics = wMetrics;
            this.expMetrics = expMetrics;
        }
    }
}
