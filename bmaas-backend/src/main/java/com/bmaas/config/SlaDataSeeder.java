package com.bmaas.config;

import com.bmaas.entity.Bug.BugPriority;
import com.bmaas.entity.SlaRule;
import com.bmaas.repository.SlaRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SlaDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SlaDataSeeder.class);
    private final SlaRuleRepository slaRuleRepository;
    private final JdbcTemplate jdbcTemplate;

    public SlaDataSeeder(SlaRuleRepository slaRuleRepository, JdbcTemplate jdbcTemplate) {
        this.slaRuleRepository = slaRuleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE bugs DROP CONSTRAINT IF EXISTS bugs_status_check;");
            jdbcTemplate.execute("ALTER TABLE bugs ADD CONSTRAINT bugs_status_check CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED', 'REJECTED'));");
        } catch (Exception e) {
            logger.warn("Could not update bugs_status_check constraint: {}", e.getMessage());
        }

        if (slaRuleRepository.count() == 0) {
            logger.info("Seeding default SLA rules into database...");
            slaRuleRepository.save(new SlaRule(BugPriority.CRITICAL, 4, 80));
            slaRuleRepository.save(new SlaRule(BugPriority.HIGH, 12, 80));
            slaRuleRepository.save(new SlaRule(BugPriority.MEDIUM, 24, 80));
            slaRuleRepository.save(new SlaRule(BugPriority.LOW, 48, 80));
            logger.info("Default SLA rules seeded successfully (CRITICAL=4h, HIGH=12h, MEDIUM=24h, LOW=48h)");
        }
    }
}
