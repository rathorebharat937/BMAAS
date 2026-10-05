package com.bmaas.service;

import com.bmaas.dto.developer.DeveloperRequest;
import com.bmaas.dto.developer.DeveloperResponse;
import com.bmaas.dto.module.ModuleMemberResponse;

import java.util.List;

public interface DeveloperService {

    List<DeveloperResponse> getAllDevelopers();

    DeveloperResponse getDeveloperById(Long id);

    DeveloperResponse createDeveloper(DeveloperRequest request);

    DeveloperResponse updateDeveloper(Long id, DeveloperRequest request);

    void deleteDeveloper(Long id);

    /** Map developer to module (no role — kept for backward compat) */
    void mapDeveloperToModule(Long moduleId, Long developerId);

    /** Map developer to module with optional role on this module */
    void mapDeveloperToModule(Long moduleId, Long developerId, String roleOnModule);

    void unmapDeveloperFromModule(Long moduleId, Long developerId);

    /** Returns plain DeveloperResponse list (used by existing getDevelopersByModule) */
    List<DeveloperResponse> getDevelopersByModule(Long moduleId);

    /** Returns enriched member list with roleOnModule included */
    List<ModuleMemberResponse> getModuleMembers(Long moduleId);

    DeveloperResponse linkUser(Long developerId, Long userId);

    DeveloperResponse getMyProfile(Long userId);
}
