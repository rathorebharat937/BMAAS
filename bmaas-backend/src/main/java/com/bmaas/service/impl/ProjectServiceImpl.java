package com.bmaas.service.impl;

import com.bmaas.dto.project.ProjectRequest;
import com.bmaas.dto.project.ProjectResponse;
import com.bmaas.entity.Project;
import com.bmaas.entity.User;
import com.bmaas.exception.ResourceNotFoundException;
import com.bmaas.repository.ProjectRepository;
import com.bmaas.repository.UserRepository;
import com.bmaas.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjectsByManager(Long managerId) {
        return projectRepository.findByProjectManagerId(managerId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapToResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id, Long managerId) {
        Project project = projectRepository.findByIdAndProjectManagerId(id, managerId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapToResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, Long managerId) {
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setTechnologyStack(request.getTechnologyStack());
        project.setStatus(Project.ProjectStatus.valueOf(request.getStatus()));
        project.setProjectManager(manager);

        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long managerId) {
        Project project = projectRepository.findByIdAndProjectManagerId(id, managerId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setTechnologyStack(request.getTechnologyStack());
        project.setStatus(Project.ProjectStatus.valueOf(request.getStatus()));

        Project updatedProject = projectRepository.save(project);
        return mapToResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long id, Long managerId) {
        Project project = projectRepository.findByIdAndProjectManagerId(id, managerId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setTechnologyStack(project.getTechnologyStack());
        response.setStatus(project.getStatus().name());
        response.setProjectManagerId(project.getProjectManager().getId());
        response.setProjectManagerName(project.getProjectManager().getFullName());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }
}