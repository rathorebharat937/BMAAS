package com.bmaas.config;

import com.bmaas.entity.Developer;
import com.bmaas.repository.DeveloperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * DeveloperDataSeeder — Part B
 *
 * Seeds 14 realistic dummy developers on startup if no developers exist.
 * Idempotent: skips seeding if any developers are already in the DB.
 * Gated: only runs in local/dev (no production check needed — controlled
 * by the ddl-auto=update / no dedicated data.sql in prod profile).
 *
 * Profiles spread:
 *   - Junior  (~0.5 – 1.5 yrs): EMP-001, EMP-007, EMP-011
 *   - Mid     (~2.0 – 4.0 yrs): EMP-002, EMP-004, EMP-006, EMP-009, EMP-012
 *   - Senior  (~5.0 – 8.0 yrs): EMP-003, EMP-005, EMP-008, EMP-010, EMP-013, EMP-014
 *
 * EMP-012 and EMP-013 are marked available=false (simulating leave).
 * No User/login accounts are created — consistent with Phase 1/4's
 * "PM adds developer record first, links account later" model.
 *
 * NOTE: These fields are structural prep for Phase 6 scoring/queue logic.
 * NO scoring or eligibility calculations are implemented here.
 *
 * Runs @Order(2) so SlaDataSeeder (@Order(1) default) runs first.
 */
@Component
@Order(2)
public class DeveloperDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DeveloperDataSeeder.class);

    private final DeveloperRepository developerRepository;

    public DeveloperDataSeeder(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @Override
    public void run(String... args) {
        if (developerRepository.existsByEmployeeId("EMP-001")) {
            logger.info("Dummy developers already seeded — skipping seed.");
            return;
        }

        logger.info("Seeding 14 dummy developers...");

        List<Developer> developers = List.of(

            // ── JUNIOR DEVELOPERS (0.5 – 1.5 yrs) ────────────────────────────
            build("Aarav Sharma",       "aarav.sharma@bmaas.com",       "EMP-001",
                  "Junior Backend Developer",  "Java, Spring Boot",
                  "Java, Spring Boot, PostgreSQL", "Engineering", 1.0,
                  LocalDate.of(2025, 9, 1), true),

            build("Ishaan Mehta",       "ishaan.mehta@bmaas.com",       "EMP-007",
                  "Junior Frontend Developer", "React, JavaScript",
                  "React, JavaScript, HTML/CSS", "Engineering", 0.5,
                  LocalDate.of(2026, 4, 15), true),

            build("Priya Nair",         "priya.nair@bmaas.com",         "EMP-011",
                  "Junior QA Engineer",        "Manual Testing, Selenium",
                  "Selenium, TestNG, Manual Testing", "QA", 1.5,
                  LocalDate.of(2025, 5, 1), true),

            // ── MID-LEVEL DEVELOPERS (2 – 4 yrs) ──────────────────────────────
            build("Riya Patel",         "riya.patel@bmaas.com",         "EMP-002",
                  "Frontend Developer",        "React, TypeScript, CSS",
                  "React, TypeScript, Tailwind CSS, Redux", "Engineering", 3.0,
                  LocalDate.of(2023, 6, 10), true),

            build("Kabir Singh",        "kabir.singh@bmaas.com",        "EMP-004",
                  "Full Stack Developer",      "Java, React, PostgreSQL",
                  "Java, Spring Boot, React, PostgreSQL", "Engineering", 2.5,
                  LocalDate.of(2024, 1, 20), true),

            build("Ananya Iyer",        "ananya.iyer@bmaas.com",        "EMP-006",
                  "Database Engineer",         "PostgreSQL, Oracle, SQL",
                  "PostgreSQL, Oracle, PL/SQL, Redis", "Data", 4.0,
                  LocalDate.of(2022, 8, 5), true),

            build("Siddharth Rao",      "siddharth.rao@bmaas.com",      "EMP-009",
                  "QA Automation Engineer",    "Selenium, Playwright, Java",
                  "Selenium, Playwright, Java, TestNG, CI/CD", "QA", 3.5,
                  LocalDate.of(2023, 2, 14), true),

            // EMP-012: available=false (on leave)
            build("Divya Krishnan",     "divya.krishnan@bmaas.com",     "EMP-012",
                  "Backend Developer",         "Java, Kafka, Microservices",
                  "Java, Spring Boot, Kafka, Docker", "Engineering", 2.0,
                  LocalDate.of(2024, 7, 1), false),

            // ── SENIOR DEVELOPERS (5 – 8 yrs) ─────────────────────────────────
            build("Vikram Joshi",       "vikram.joshi@bmaas.com",       "EMP-003",
                  "Senior Backend Developer",  "Java, Microservices, AWS",
                  "Java, Spring Boot, Microservices, AWS, Docker, Kubernetes", "Engineering", 7.0,
                  LocalDate.of(2019, 3, 15), true),

            build("Meera Reddy",        "meera.reddy@bmaas.com",        "EMP-005",
                  "Senior Full Stack Developer","Java, React, AWS, PostgreSQL",
                  "Java, React, TypeScript, PostgreSQL, AWS, Redis", "Engineering", 6.5,
                  LocalDate.of(2019, 11, 1), true),

            build("Arjun Verma",        "arjun.verma@bmaas.com",        "EMP-008",
                  "Senior DevOps Engineer",    "AWS, Docker, Kubernetes, CI/CD",
                  "AWS, Docker, Kubernetes, Jenkins, Terraform, Ansible", "DevOps", 8.0,
                  LocalDate.of(2018, 5, 20), true),

            build("Neha Gupta",         "neha.gupta@bmaas.com",         "EMP-010",
                  "Senior QA Lead",            "Selenium, JMeter, API Testing",
                  "Selenium, JMeter, Postman, API Testing, Test Strategy", "QA", 5.5,
                  LocalDate.of(2021, 1, 10), true),

            // EMP-013: available=false (on leave)
            build("Rohan Desai",        "rohan.desai@bmaas.com",        "EMP-013",
                  "Senior Database Architect", "PostgreSQL, Oracle, MongoDB, Redis",
                  "PostgreSQL, MongoDB, Oracle, Redis, Data Modelling", "Data", 6.0,
                  LocalDate.of(2020, 7, 15), false),

            build("Tanvi Bhatia",       "tanvi.bhatia@bmaas.com",       "EMP-014",
                  "Tech Lead / Senior Full Stack","Java, React, System Design",
                  "Java, Spring Boot, React, PostgreSQL, System Design, Leadership", "Engineering", 7.5,
                  LocalDate.of(2018, 10, 1), true)
        );

        developerRepository.saveAll(developers);
        logger.info("Seeded {} developers successfully.", developers.size());

        // Log summary for delivery report
        developers.forEach(d ->
            logger.info("  [{}] {} — {} | {}yrs | {} | available={}",
                d.getEmployeeId(), d.getName(), d.getJobTitle(),
                d.getYearsOfExperience(), d.getDepartment(), d.isAvailable())
        );
    }

    private Developer build(String name, String email, String employeeId,
                             String jobTitle, String primarySkills,
                             String skillsTechStack, String department,
                             double yearsOfExperience, LocalDate dateJoined,
                             boolean available) {
        Developer d = new Developer();
        d.setName(name);
        d.setEmail(email);
        d.setEmployeeId(employeeId);
        d.setJobTitle(jobTitle);
        d.setPrimarySkills(primarySkills);
        d.setSkillsTechStack(skillsTechStack);   // keep original field populated too
        d.setDepartment(department);
        d.setYearsOfExperience(yearsOfExperience);
        d.setDateJoined(dateJoined);
        d.setAvailable(available);
        // userId intentionally left null — PM links accounts later (Phase 4 flow)
        return d;
    }
}
