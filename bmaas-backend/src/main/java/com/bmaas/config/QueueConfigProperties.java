package com.bmaas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bmaas.assignment")
public class QueueConfigProperties {

    private Weights weights = new Weights();
    private WorkloadFactors workloadFactors = new WorkloadFactors();
    private double experienceCapYears = 5.0;

    public static class Weights {
        private double workload = 0.60;
        private double experience = 0.40;

        public double getWorkload() { return workload; }
        public void setWorkload(double workload) { this.workload = workload; }

        public double getExperience() { return experience; }
        public void setExperience(double experience) { this.experience = experience; }
    }

    public static class WorkloadFactors {
        private double project = 0.20;
        private double module = 0.30;
        private double bug = 0.50;

        public double getProject() { return project; }
        public void setProject(double project) { this.project = project; }

        public double getModule() { return module; }
        public void setModule(double module) { this.module = module; }

        public double getBug() { return bug; }
        public void setBug(double bug) { this.bug = bug; }
    }

    public Weights getWeights() { return weights; }
    public void setWeights(Weights weights) { this.weights = weights; }

    public WorkloadFactors getWorkloadFactors() { return workloadFactors; }
    public void setWorkloadFactors(WorkloadFactors workloadFactors) { this.workloadFactors = workloadFactors; }

    public double getExperienceCapYears() { return experienceCapYears; }
    public void setExperienceCapYears(double experienceCapYears) { this.experienceCapYears = experienceCapYears; }
}
