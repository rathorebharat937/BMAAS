package com.bmaas.service;

import com.bmaas.dto.module.ModuleRequest;
import com.bmaas.dto.module.ModuleResponse;

import java.util.List;

public interface ModuleService {

    List<ModuleResponse> getAllModulesByProject(Long projectId);

    ModuleResponse getModuleById(Long id);

    ModuleResponse createModule(Long projectId, ModuleRequest request);

    ModuleResponse updateModule(Long id, ModuleRequest request);

    void deleteModule(Long id);
}
