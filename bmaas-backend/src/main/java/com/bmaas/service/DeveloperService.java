package com.bmaas.service;

import com.bmaas.dto.developer.DeveloperRequest;
import com.bmaas.dto.developer.DeveloperResponse;

import java.util.List;

public interface DeveloperService {

    List<DeveloperResponse> getAllDevelopers();

    DeveloperResponse getDeveloperById(Long id);

    DeveloperResponse createDeveloper(DeveloperRequest request);

    DeveloperResponse updateDeveloper(Long id, DeveloperRequest request);

    void deleteDeveloper(Long id);

    void mapDeveloperToModule(Long moduleId, Long developerId);

    void unmapDeveloperFromModule(Long moduleId, Long developerId);

    List<DeveloperResponse> getDevelopersByModule(Long moduleId);

    DeveloperResponse linkUser(Long developerId, Long userId);

    DeveloperResponse getMyProfile(Long userId);
}
