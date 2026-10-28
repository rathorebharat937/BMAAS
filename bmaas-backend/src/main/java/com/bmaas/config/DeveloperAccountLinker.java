package com.bmaas.config;

import com.bmaas.entity.Developer;
import com.bmaas.entity.User;
import com.bmaas.repository.DeveloperRepository;
import com.bmaas.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * DeveloperAccountLinker — Runs AFTER DeveloperDataSeeder (Order 3).
 *
 * Ensures every Developer with an @bmaas.com email (EMP-* employees)
 * has a linked User account with:
 *   - username = their email
 *   - password = Bharat@123 (BCrypt hashed)
 *   - role     = DEVELOPER
 *
 * Also normalizes passwords for any already-linked developer accounts
 * so all developers can login with the same password.
 */
@Component
@Order(3)
public class DeveloperAccountLinker implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DeveloperAccountLinker.class);
    private static final String COMMON_PASSWORD = "Bharat@123";

    private final DeveloperRepository developerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public DeveloperAccountLinker(DeveloperRepository developerRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  Environment environment) {
        this.developerRepository = developerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        boolean isProd = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));

        if (isProd) {
            logger.info("Production profile — DeveloperAccountLinker disabled.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(COMMON_PASSWORD);

        // Fetch all developers
        List<Developer> allDevelopers = developerRepository.findAll();
        int created = 0;
        int updated = 0;
        int alreadyLinked = 0;

        for (Developer dev : allDevelopers) {
            String email = dev.getEmail();
            if (email == null || email.isBlank()) continue;

            if (dev.getUser() != null) {
                // Developer already has a linked user — update password to common password
                User existingUser = dev.getUser();
                existingUser.setPassword(encodedPassword);
                userRepository.save(existingUser);
                updated++;
                logger.debug("[LINKER] Updated password for existing user: {} (dev: {})", existingUser.getUsername(), dev.getName());
                continue;
            }

            // Check if a User with this email already exists but is not linked
            User existingUser = userRepository.findByEmail(email).orElse(null);

            if (existingUser != null) {
                // Link existing user to developer and update password
                existingUser.setPassword(encodedPassword);
                if (existingUser.getRole() != User.UserRole.DEVELOPER) {
                    existingUser.setRole(User.UserRole.DEVELOPER);
                }
                userRepository.save(existingUser);

                dev.setUser(existingUser);
                developerRepository.save(dev);
                alreadyLinked++;
                logger.info("[LINKER] Linked existing user '{}' to developer '{}'", existingUser.getUsername(), dev.getName());
                continue;
            }

            // Check if username (email) is already taken by another user
            if (userRepository.existsByUsername(email)) {
                User byUsername = userRepository.findByUsername(email).orElse(null);
                if (byUsername != null) {
                    byUsername.setPassword(encodedPassword);
                    userRepository.save(byUsername);

                    dev.setUser(byUsername);
                    developerRepository.save(dev);
                    alreadyLinked++;
                    logger.info("[LINKER] Linked user (by username) '{}' to developer '{}'", email, dev.getName());
                }
                continue;
            }

            // Create new User account for this developer
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setPassword(encodedPassword);
            newUser.setFullName(dev.getName());
            newUser.setRole(User.UserRole.DEVELOPER);
            newUser = userRepository.save(newUser);

            dev.setUser(newUser);
            developerRepository.save(dev);

            created++;
            logger.info("[LINKER] Created user account for developer '{}' — username: {}, password: {}",
                    dev.getName(), email, COMMON_PASSWORD);
        }

        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("  DeveloperAccountLinker Summary:");
        logger.info("  Total developers processed: {}", allDevelopers.size());
        logger.info("  New accounts created:       {}", created);
        logger.info("  Existing passwords updated: {}", updated);
        logger.info("  Existing users linked:      {}", alreadyLinked);
        logger.info("  Common password for ALL:    {}", COMMON_PASSWORD);
        logger.info("  Login with: username = developer's email, password = {}", COMMON_PASSWORD);
        logger.info("═══════════════════════════════════════════════════════════════");
    }
}
