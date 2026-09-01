package com.bmaas.service.impl;

import com.bmaas.dto.module.ModuleRequest;
import com.bmaas.dto.module.ModuleResponse;
import com.bmaas.entity.Module;
import com.bmaas.entity.Project;
import com.bmaas.exception.ResourceNotFoundException;
import com.bmaas.repository.ModuleRepository;
import com.bmaas.repository.ProjectRepository;
import com.bmaas.service.ModuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ProjectRepository projectRepository;

    public ModuleServiceImpl(ModuleRepository moduleRepository, ProjectRepository projectRepository) {
        this.moduleRepository = moduleRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleResponse> getAllModulesByProject(Long projectId) {
        return moduleRepository.findByProjectId(projectId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleResponse getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
        return mapToResponse(module);
    }

    @Override
    @Transactional
    public ModuleResponse createModule(Long projectId, ModuleRequest request) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Module module = new Module();
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setTechnology(request.getTechnology());
        module.setStatus(Module.ModuleStatus.valueOf(request.getStatus()));
        module.setPriority(Module.ModulePriority.valueOf(request.getPriority()));
        module.setProject(project);

        Module savedModule = moduleRepository.save(module);
        return mapToResponse(savedModule);
    }

    @Override
    @Transactional
    public ModuleResponse updateModule(Long id, ModuleRequest request) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));

        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setTechnology(request.getTechnology());
        module.setStatus(Module.ModuleStatus.valueOf(request.getStatus()));
        module.setPriority(Module.ModulePriority.valueOf(request.getPriority()));

        Module updatedModule = moduleRepository.save(module);
        return mapToResponse(updatedModule);
    }

    @Override
    @Transactional
    public void deleteModule(Long id) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
        moduleRepository.delete(module);
    }

    private ModuleResponse mapToResponse(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.setId(module.getId());
        response.setName(module.getName());
        response.setDescription(module.getDescription());
        response.setTechnology(module.getTechnology());
        response.setStatus(module.getStatus().name());
        response.setPriority(module.getPriority().name());
        response.setProjectId(module.getProject().getId());
        response.setProjectName(module.getProject().getName());
        response.setDeveloperCount(module.getDevelopers().size());
        response.setCreatedAt(module.getCreatedAt());
        response.setUpdatedAt(module.getUpdatedAt());
        return response;
    }
}