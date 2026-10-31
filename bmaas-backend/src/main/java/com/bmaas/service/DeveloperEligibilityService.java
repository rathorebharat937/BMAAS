package com.bmaas.service;

import com.bmaas.entity.Developer;
import com.bmaas.entity.ModuleDeveloper;
import com.bmaas.repository.ModuleDeveloperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeveloperEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(DeveloperEligibilityService.class);

    private final ModuleDeveloperRepository moduleDeveloperRepository;

    public DeveloperEligibilityService(ModuleDeveloperRepository moduleDeveloperRepository) {
        this.moduleDeveloperRepository = moduleDeveloperRepository;
    }

    /**
     * Retrieve all eligible developers strictly mapped to the given module.
     * Verification conditions:
     * 1. Developer is explicitly mapped to the module (via module_developer_role).
     * 2. Developer is available (isAvailable == true).
     */
    @Transactional(readOnly = true)
    public List<ModuleDeveloper> getEligibleModuleDevelopers(Long moduleId) {
        List<ModuleDeveloper> memberships = moduleDeveloperRepository.findByModuleIdWithDeveloper(moduleId);
        List<ModuleDeveloper> eligible = new ArrayList<>();

        for (ModuleDeveloper md : memberships) {
            Developer dev = md.getDeveloper();
            if (dev == null) {
                continue;
            }

            if (!dev.isAvailable()) {
                log.info("Developer {} (id: {}) excluded from queue: marked as unavailable / on leave",
                        dev.getName(), dev.getId());
                continue;
            }

            eligible.add(md);
        }

        log.info("Found {} eligible developer(s) mapped to module id {}", eligible.size(), moduleId);
        return eligible;
    }
}
