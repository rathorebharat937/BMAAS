package com.bmaas.service;

import com.bmaas.dto.project.ProjectRequest;
import com.bmaas.dto.project.ProjectResponse;

import java.util.List;

public interface ProjectService {

    List<ProjectResponse> getAllProjects();

    List<ProjectResponse> getAllProjectsByManager(Long managerId);

    ProjectResponse getProjectById(Long id);

    ProjectResponse getProjectById(Long id, Long managerId);

    ProjectResponse createProject(ProjectRequest request, Long managerId);

    ProjectResponse updateProject(Long id, ProjectRequest request, Long managerId);

    void deleteProject(Long id, Long managerId);
}

