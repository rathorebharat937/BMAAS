package com.bmaas.service;

import com.bmaas.config.QueueConfigProperties;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.repository.BugRepository;
import com.bmaas.repository.ModuleDeveloperRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeveloperWorkloadScoringService {

    private final BugRepository bugRepository;
    private final ModuleDeveloperRepository moduleDeveloperRepository;
    private final QueueConfigProperties queueConfig;

    private static final List<BugStatus> ACTIVE_BUG_STATUSES = List.of(
            BugStatus.ASSIGNED,
            BugStatus.IN_PROGRESS,
            BugStatus.REOPENED
    );

    public DeveloperWorkloadScoringService(BugRepository bugRepository,
                                           ModuleDeveloperRepository moduleDeveloperRepository,
                                           QueueConfigProperties queueConfig) {
        this.bugRepository = bugRepository;
        this.moduleDeveloperRepository = moduleDeveloperRepository;
        this.queueConfig = queueConfig;
    }

    public static class WorkloadMetrics {
        public final int activeProjects;
        public final int activeModules;
        public final int activeBugs;
        public final double rawWorkload;

        public WorkloadMetrics(int activeProjects, int activeModules, int activeBugs, double rawWorkload) {
            this.activeProjects = activeProjects;
            this.activeModules = activeModules;
            this.activeBugs = activeBugs;
            this.rawWorkload = rawWorkload;
        }
    }

    @Transactional(readOnly = true)
    public WorkloadMetrics calculateRawWorkload(Long developerId) {
        int activeProjects = (int) moduleDeveloperRepository.countActiveProjectsByDeveloperId(developerId);
        int activeModules = (int) moduleDeveloperRepository.countActiveModulesByDeveloperId(developerId);
        int activeBugs = (int) bugRepository.countByAssignedDeveloperIdAndStatusIn(developerId, ACTIVE_BUG_STATUSES);

        QueueConfigProperties.WorkloadFactors factors = queueConfig.getWorkloadFactors();
        double rawWorkload = (factors.getProject() * activeProjects)
                + (factors.getModule() * activeModules)
                + (factors.getBug() * activeBugs);

        return new WorkloadMetrics(activeProjects, activeModules, activeBugs, rawWorkload);
    }

    /**
     * Inverted normalization: lowest raw workload gets highest score (100.0).
     */
    public double normalizeWorkloadScore(double rawWorkload, double minRawWorkload, double maxRawWorkload) {
        if (Double.compare(minRawWorkload, maxRawWorkload) == 0) {
            return 100.0;
        }
        double score = ((maxRawWorkload - rawWorkload) / (maxRawWorkload - minRawWorkload)) * 100.0;
        return Math.max(0.0, Math.min(100.0, score));
    }
}
