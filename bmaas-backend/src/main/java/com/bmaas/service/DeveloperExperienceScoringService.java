package com.bmaas.service;

import com.bmaas.config.QueueConfigProperties;
import com.bmaas.entity.Developer;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class DeveloperExperienceScoringService {

    private final QueueConfigProperties queueConfig;

    public DeveloperExperienceScoringService(QueueConfigProperties queueConfig) {
        this.queueConfig = queueConfig;
    }

    public static class ExperienceMetrics {
        public final LocalDate dateJoined;
        public final double tenureYears;
        public final double normalizedScore; // 0.0 - 100.0

        public ExperienceMetrics(LocalDate dateJoined, double tenureYears, double normalizedScore) {
            this.dateJoined = dateJoined;
            this.tenureYears = tenureYears;
            this.normalizedScore = normalizedScore;
        }
    }

    public ExperienceMetrics calculateExperience(Developer developer) {
        double capYears = queueConfig.getExperienceCapYears();
        if (capYears <= 0) {
            capYears = 5.0;
        }

        LocalDate dateJoined = developer.getDateJoined();
        double tenureYears;

        if (dateJoined != null) {
            long days = ChronoUnit.DAYS.between(dateJoined, LocalDate.now());
            tenureYears = Math.max(0.0, days / 365.25);
        } else if (developer.getYearsOfExperience() != null) {
            // Fallback to years of experience if dateJoined is not explicitly set
            tenureYears = Math.max(0.0, developer.getYearsOfExperience());
        } else {
            tenureYears = 0.0;
        }

        double cappedTenure = Math.min(tenureYears, capYears);
        double normalizedScore = (cappedTenure / capYears) * 100.0;
        normalizedScore = Math.max(0.0, Math.min(100.0, normalizedScore));

        return new ExperienceMetrics(dateJoined, tenureYears, normalizedScore);
    }
}
