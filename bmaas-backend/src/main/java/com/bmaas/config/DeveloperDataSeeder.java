package com.bmaas.config;

import com.bmaas.entity.*;
import com.bmaas.entity.Module;
import com.bmaas.entity.Bug.BugPriority;
import com.bmaas.entity.Bug.BugSeverity;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.Bug.SlaStatus;
import com.bmaas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DeveloperDataSeeder — Parts B & Part 2
 *
 * 1. Seeds 14 realistic dummy developers covering a spread of experience levels.
 * 2. Seeds 3 test developer accounts with actual User logins (Aarav, Riya, Vikram).
 * 3. Gated to non-production environments only.
 * 4. Generates secure passwords at seed time, hashed with BCrypt.
 * 5. Logs credentials to console and writes to seed-credentials.local.md (gitignored).
 * 6. Maps seeded developers to a real module and assigns sample bugs across ASSIGNED & IN_PROGRESS.
 */
@Component
@Order(2)
public class DeveloperDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DeveloperDataSeeder.class);

    private final DeveloperRepository developerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleDeveloperRepository moduleDeveloperRepository;
    private final BugRepository bugRepository;
    private final Environment environment;

    public DeveloperDataSeeder(DeveloperRepository developerRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               ProjectRepository projectRepository,
                               ModuleRepository moduleRepository,
                               ModuleDeveloperRepository moduleDeveloperRepository,
                               BugRepository bugRepository,
                               Environment environment) {
        this.developerRepository = developerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.moduleRepository = moduleRepository;
        this.moduleDeveloperRepository = moduleDeveloperRepository;
        this.bugRepository = bugRepository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        boolean isProd = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));

        if (isProd) {
            logger.info("Production profile detected — DeveloperDataSeeder disabled.");
            return;
        }

        // ── 1. Seed Dummy Developers if not present ─────────────────────────
        seedDummyDevelopersIfNeeded();

        // ── 2. Seed 3 Test Developer User Accounts with real logins ──────────
        seedTestDeveloperUsers();
    }

    private void seedDummyDevelopersIfNeeded() {
        if (developerRepository.existsByEmployeeId("EMP-001")) {
            logger.info("Dummy developers already seeded.");
            return;
        }

        logger.info("Seeding 14 dummy developers...");

        List<Developer> developers = List.of(
            // Junior (0.5 – 1.5 yrs)
            build("Aarav Sharma",   "aarav.sharma@bmaas.com",   "EMP-001",
                  "Junior Backend Developer",  "Java, Spring Boot",
                  "Java, Spring Boot, PostgreSQL", "Engineering", 1.0,
                  LocalDate.of(2025, 9, 1), true),

            build("Ishaan Mehta",   "ishaan.mehta@bmaas.com",   "EMP-007",
                  "Junior Frontend Developer", "React, JavaScript",
                  "React, JavaScript, HTML/CSS", "Engineering", 0.5,
                  LocalDate.of(2026, 4, 15), true),

            build("Priya Nair",     "priya.nair@bmaas.com",     "EMP-011",
                  "Junior QA Engineer",        "Manual Testing, Selenium",
                  "Selenium, TestNG, Manual Testing", "QA", 1.5,
                  LocalDate.of(2025, 5, 1), true),

            // Mid-level (2 – 4 yrs)
            build("Riya Patel",     "riya.patel@bmaas.com",     "EMP-002",
                  "Frontend Developer",        "React, TypeScript, CSS",
                  "React, TypeScript, Tailwind CSS, Redux", "Engineering", 3.0,
                  LocalDate.of(2023, 6, 10), true),

            build("Kabir Singh",    "kabir.singh@bmaas.com",    "EMP-004",
                  "Full Stack Developer",      "Java, React, PostgreSQL",
                  "Java, Spring Boot, React, PostgreSQL", "Engineering", 2.5,
                  LocalDate.of(2024, 1, 20), true),

            build("Ananya Iyer",    "ananya.iyer@bmaas.com",    "EMP-006",
                  "Database Engineer",         "PostgreSQL, Oracle, SQL",
                  "PostgreSQL, Oracle, PL/SQL, Redis", "Data", 4.0,
                  LocalDate.of(2022, 8, 5), true),

            build("Siddharth Rao",  "siddharth.rao@bmaas.com",  "EMP-009",
                  "QA Automation Engineer",    "Selenium, Playwright, Java",
                  "Selenium, Playwright, Java, TestNG, CI/CD", "QA", 3.5,
                  LocalDate.of(2023, 2, 14), true),

            build("Divya Krishnan", "divya.krishnan@bmaas.com", "EMP-012",
                  "Backend Developer",         "Java, Kafka, Microservices",
                  "Java, Spring Boot, Kafka, Docker", "Engineering", 2.0,
                  LocalDate.of(2024, 7, 1), false),

            // Senior (5 – 8 yrs)
            build("Vikram Joshi",   "vikram.joshi@bmaas.com",   "EMP-003",
                  "Senior Backend Developer",  "Java, Microservices, AWS",
                  "Java, Spring Boot, Microservices, AWS, Docker, Kubernetes", "Engineering", 7.0,
                  LocalDate.of(2019, 3, 15), true),

            build("Meera Reddy",    "meera.reddy@bmaas.com",    "EMP-005",
                  "Senior Full Stack Developer","Java, React, AWS, PostgreSQL",
                  "Java, React, TypeScript, PostgreSQL, AWS, Redis", "Engineering", 6.5,
                  LocalDate.of(2019, 11, 1), true),

            build("Arjun Verma",    "arjun.verma@bmaas.com",    "EMP-008",
                  "Senior DevOps Engineer",    "AWS, Docker, Kubernetes, CI/CD",
                  "AWS, Docker, Kubernetes, Jenkins, Terraform, Ansible", "DevOps", 8.0,
                  LocalDate.of(2018, 5, 20), true),

            build("Neha Gupta",     "neha.gupta@bmaas.com",     "EMP-010",
                  "Senior QA Lead",            "Selenium, JMeter, API Testing",
                  "Selenium, JMeter, Postman, API Testing, Test Strategy", "QA", 5.5,
                  LocalDate.of(2021, 1, 10), true),

            build("Rohan Desai",    "rohan.desai@bmaas.com",    "EMP-013",
                  "Senior Database Architect", "PostgreSQL, Oracle, MongoDB, Redis",
                  "PostgreSQL, MongoDB, Oracle, Redis, Data Modelling", "Data", 6.0,
                  LocalDate.of(2020, 7, 15), false),

            build("Tanvi Bhatia",   "tanvi.bhatia@bmaas.com",   "EMP-014",
                  "Tech Lead / Senior Full Stack","Java, React, System Design",
                  "Java, Spring Boot, React, PostgreSQL, System Design, Leadership", "Engineering", 7.5,
                  LocalDate.of(2018, 10, 1), true)
        );

        developerRepository.saveAll(developers);
        logger.info("Seeded {} developers successfully.", developers.size());
    }

    private void seedTestDeveloperUsers() {
        // Target 3 seeded developers across experience levels
        List<String> targetEmails = List.of(
            "vikram.joshi@bmaas.com", // Senior (EMP-003)
            "riya.patel@bmaas.com",   // Mid (EMP-002)
            "aarav.sharma@bmaas.com"  // Junior (EMP-001)
        );

        List<String> credentialLines = new ArrayList<>();
        credentialLines.add("# BMAAS Seeded Test Developer Credentials (Local/Dev Only)");
        credentialLines.add("Generated: " + LocalDateTime.now());
        credentialLines.add("");
        credentialLines.add("| Name | Email (Username) | Password | Role | Employee ID | Experience |");
        credentialLines.add("| :--- | :--- | :--- | :--- | :--- | :--- |");

        for (String email : targetEmails) {
            Optional<Developer> devOpt = developerRepository.findByEmailIgnoreCase(email);
            if (devOpt.isEmpty()) continue;
            Developer dev = devOpt.get();

            Optional<User> userOpt = userRepository.findByEmail(email);
            String rawPassword;
            User user;

            if (userOpt.isEmpty()) {
                // Generate secure test password at seed time (not hardcoded)
                rawPassword = "Dev@" + UUID.randomUUID().toString().substring(0, 8);
                user = new User();
                user.setUsername(email);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setFullName(dev.getName());
                user.setRole(User.UserRole.DEVELOPER);
                user = userRepository.save(user);

                dev.setUser(user);
                developerRepository.save(dev);

                logger.info("[DEV SEED] Test developer login: email={} username={} password={}",
                        email, email, rawPassword);
                credentialLines.add(String.format("| %s | %s | `%s` | DEVELOPER | %s | %.1f yrs |",
                        dev.getName(), email, rawPassword, dev.getEmployeeId(), dev.getYearsOfExperience()));
            } else {
                user = userOpt.get();
                if (dev.getUser() == null) {
                    dev.setUser(user);
                    developerRepository.save(dev);
                }
                logger.info("[DEV SEED] Test developer user already exists: email={}", email);
            }
        }

        // Write credentials to gitignored local file
        writeCredentialsFile(credentialLines);

        // Ensure module assignment and sample bugs for Vikram Joshi
        setupModuleAndSampleBugs();
    }

    private void writeCredentialsFile(List<String> lines) {
        try {
            // Write to project root
            Path path = Paths.get("..", "seed-credentials.local.md");
            if (!Files.exists(path.getParent()) || !Files.isDirectory(path.getParent())) {
                path = Paths.get("seed-credentials.local.md");
            }
            try (FileWriter writer = new FileWriter(path.toFile())) {
                for (String line : lines) {
                    writer.write(line + System.lineSeparator());
                }
            }
            logger.info("[DEV SEED] Credentials written to: {}", path.toAbsolutePath());
        } catch (IOException e) {
            logger.warn("Could not write seed-credentials.local.md: {}", e.getMessage());
        }
    }

    private void setupModuleAndSampleBugs() {
        Developer vikram = developerRepository.findByEmailIgnoreCase("vikram.joshi@bmaas.com").orElse(null);
        if (vikram == null) return;

        // Find or create module for testing
        Module testModule = moduleRepository.findAll().stream().findFirst().orElse(null);
        if (testModule == null) {
            Project testProj = new Project();
            testProj.setName("Core Platform");
            testProj.setDescription("Primary development project for testing");
            testProj.setStatus(Project.ProjectStatus.ACTIVE);
            testProj.setStartDate(LocalDate.now().minusMonths(1));
            testProj = projectRepository.save(testProj);

            testModule = new Module();
            testModule.setName("Auth & Session Engine");
            testModule.setDescription("Handles authentication, JWT verification, and active sessions");
            testModule.setProject(testProj);
            testModule.setStatus(Module.ModuleStatus.IN_PROGRESS);
            testModule.setPriority(Module.ModulePriority.HIGH);
            testModule = moduleRepository.save(testModule);
        }

        // Map Vikram to module if not already mapped
        if (!moduleDeveloperRepository.existsByModuleIdAndDeveloperId(testModule.getId(), vikram.getId())) {
            ModuleDeveloper md = new ModuleDeveloper(testModule, vikram, "Lead Backend Architect");
            moduleDeveloperRepository.save(md);
            logger.info("Mapped Vikram Joshi to module '{}' with role 'Lead Backend Architect'", testModule.getName());
        }

        // Also map Aarav and Riya
        Developer aarav = developerRepository.findByEmailIgnoreCase("aarav.sharma@bmaas.com").orElse(null);
        if (aarav != null && !moduleDeveloperRepository.existsByModuleIdAndDeveloperId(testModule.getId(), aarav.getId())) {
            moduleDeveloperRepository.save(new ModuleDeveloper(testModule, aarav, "Backend Contributor"));
        }
        Developer riya = developerRepository.findByEmailIgnoreCase("riya.patel@bmaas.com").orElse(null);
        if (riya != null && !moduleDeveloperRepository.existsByModuleIdAndDeveloperId(testModule.getId(), riya.getId())) {
            moduleDeveloperRepository.save(new ModuleDeveloper(testModule, riya, "Frontend Engineer"));
        }

        // Check if sample bugs for Vikram already exist
        List<Bug> existingBugs = bugRepository.findByAssignedDeveloperWithFilters(vikram.getId(), null, null, null, null);
        if (existingBugs.size() >= 2) {
            logger.info("Sample bugs already exist for Vikram Joshi (count={})", existingBugs.size());
            return;
        }

        // Find or create a reporter user (e.g. a TESTER)
        User reporter = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.TESTER || u.getRole() == User.UserRole.PROJECT_MANAGER)
                .findFirst()
                .orElse(null);

        if (reporter == null) {
            reporter = new User();
            reporter.setUsername("qa_lead_seed@bmaas.com");
            reporter.setEmail("qa_lead_seed@bmaas.com");
            reporter.setPassword(passwordEncoder.encode("Test@1234"));
            reporter.setFullName("QA Lead Tester");
            reporter.setRole(User.UserRole.TESTER);
            reporter = userRepository.save(reporter);
        }

        logger.info("Creating sample bugs for Vikram Joshi on module '{}'...", testModule.getName());

        // Bug 1: Status = ASSIGNED (Pending Action)
        Bug bug1 = new Bug();
        bug1.setTitle("[Sample] Session timeout redirects to 404 instead of login");
        bug1.setDescription("When JWT token expires, the client receives HTTP 401 but the frontend router falls back to 404 error page rather than cleanly redirecting to the login screen.");
        bug1.setProject(testModule.getProject());
        bug1.setModule(testModule);
        bug1.setSeverity(BugSeverity.HIGH);
        bug1.setPriority(BugPriority.HIGH);
        bug1.setStatus(BugStatus.ASSIGNED);
        bug1.setSlaStatus(SlaStatus.ON_TRACK);
        bug1.setSlaDeadline(LocalDateTime.now().plusHours(24));
        bug1.setAssignedDeveloper(vikram);
        bug1.setAssignedAt(LocalDateTime.now().minusHours(2));
        bug1.setReporter(reporter);
        bug1.setEnvironment("Staging / Chrome 122");
        bug1.setExpectedResult("Redirect to /login with session expired toast notification");
        bug1.setActualResult("404 Not Found error view is displayed");
        bugRepository.save(bug1);

        // Bug 2: Status = IN_PROGRESS (Active Work)
        Bug bug2 = new Bug();
        bug2.setTitle("[Sample] Payment webhook signature verification fails under high concurrency");
        bug2.setDescription("Concurrent webhook events from Stripe fail HMAC signature validation due to request body stream consumption.");
        bug2.setProject(testModule.getProject());
        bug2.setModule(testModule);
        bug2.setSeverity(BugSeverity.CRITICAL);
        bug2.setPriority(BugPriority.CRITICAL);
        bug2.setStatus(BugStatus.IN_PROGRESS);
        bug2.setSlaStatus(SlaStatus.AT_RISK);
        bug2.setSlaDeadline(LocalDateTime.now().plusHours(4));
        bug2.setAssignedDeveloper(vikram);
        bug2.setAssignedAt(LocalDateTime.now().minusHours(6));
        bug2.setReporter(reporter);
        bug2.setEnvironment("Production-mirror / OpenJDK 21");
        bug2.setExpectedResult("HMAC verification succeeds for all genuine signed webhooks");
        bug2.setActualResult("HMAC signature mismatch thrown for 8% of requests");
        bugRepository.save(bug2);

        logger.info("Created 2 sample bugs (ASSIGNED & IN_PROGRESS) assigned to Vikram Joshi (EMP-003)");
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
        d.setSkillsTechStack(skillsTechStack);
        d.setDepartment(department);
        d.setYearsOfExperience(yearsOfExperience);
        d.setDateJoined(dateJoined);
        d.setAvailable(available);
        return d;
    }
}

