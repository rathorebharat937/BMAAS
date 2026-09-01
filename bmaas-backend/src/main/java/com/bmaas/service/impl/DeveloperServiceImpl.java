package com.bmaas.service.impl;

import com.bmaas.dto.developer.DeveloperRequest;
import com.bmaas.dto.developer.DeveloperResponse;
import com.bmaas.entity.Developer;
import com.bmaas.entity.Module;
import com.bmaas.entity.User;
import com.bmaas.exception.ResourceAlreadyExistsException;
import com.bmaas.exception.ResourceNotFoundException;
import com.bmaas.repository.DeveloperRepository;
import com.bmaas.repository.ModuleRepository;
import com.bmaas.repository.UserRepository;
import com.bmaas.service.DeveloperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final com.bmaas.repository.DeveloperMetricsRepository metricsRepository;
    private final com.bmaas.service.WorkloadService workloadService;

    public DeveloperServiceImpl(DeveloperRepository developerRepository, ModuleRepository moduleRepository,
                                UserRepository userRepository,
                                com.bmaas.repository.DeveloperMetricsRepository metricsRepository,
                                com.bmaas.service.WorkloadService workloadService) {
        this.developerRepository = developerRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.metricsRepository = metricsRepository;
        this.workloadService = workloadService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperResponse> getAllDevelopers() {
        return developerRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperResponse getDeveloperById(Long id) {
        Developer developer = developerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        return mapToResponse(developer);
    }

    @Override
    @Transactional
    public DeveloperResponse createDeveloper(DeveloperRequest request) {
        if (developerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Developer with email " + request.getEmail() + " already exists");
        }

        Developer developer = new Developer();
        developer.setName(request.getName());
        developer.setEmail(request.getEmail());
        developer.setSkillsTechStack(request.getSkillsTechStack());

        // Check if a User already exists with this email and role DEVELOPER
        Optional<User> matchingUser = userRepository.findByEmail(request.getEmail());
        if (matchingUser.isPresent() && matchingUser.get().getRole() == User.UserRole.DEVELOPER) {
            developer.setUser(matchingUser.get());
        }

        Developer savedDeveloper = developerRepository.save(developer);
        return mapToResponse(savedDeveloper);
    }

    @Override
    @Transactional
    public DeveloperResponse updateDeveloper(Long id, DeveloperRequest request) {
        Developer developer = developerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));

        if (!developer.getEmail().equals(request.getEmail()) && 
            developerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Developer with email " + request.getEmail() + " already exists");
        }

        developer.setName(request.getName());
        developer.setEmail(request.getEmail());
        developer.setSkillsTechStack(request.getSkillsTechStack());

        Developer updatedDeveloper = developerRepository.save(developer);
        return mapToResponse(updatedDeveloper);
    }

    @Override
    @Transactional
    public void deleteDeveloper(Long id) {
        Developer developer = developerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + id));
        developerRepository.delete(developer);
    }

    @Override
    @Transactional
    public void mapDeveloperToModule(Long moduleId, Long developerId) {
        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        Developer developer = developerRepository.findById(developerId)
            .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + developerId));

        if (!module.getDevelopers().contains(developer)) {
            module.getDevelopers().add(developer);
            moduleRepository.save(module);
        }
    }

    @Override
    @Transactional
    public void unmapDeveloperFromModule(Long moduleId, Long developerId) {
        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        Developer developer = developerRepository.findById(developerId)
            .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + developerId));

        module.getDevelopers().remove(developer);
        moduleRepository.save(module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeveloperResponse> getDevelopersByModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        return module.getDevelopers()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeveloperResponse linkUser(Long developerId, Long userId) {
        Developer developer = developerRepository.findById(developerId)
            .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + developerId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != User.UserRole.DEVELOPER) {
            throw new IllegalArgumentException("User " + user.getUsername() + " does not have the DEVELOPER role");
        }

        Optional<Developer> existing = developerRepository.findByUserId(userId);
        if (existing.isPresent() && !existing.get().getId().equals(developerId)) {
            throw new ResourceAlreadyExistsException("User is already linked to developer: " + existing.get().getName());
        }

        developer.setUser(user);
        Developer saved = developerRepository.save(developer);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeveloperResponse getMyProfile(Long userId) {
        return developerRepository.findByUserId(userId)
            .map(this::mapToResponse)
            .orElse(null);
    }

    private DeveloperResponse mapToResponse(Developer developer) {
        DeveloperResponse response = new DeveloperResponse();
        response.setId(developer.getId());
        response.setName(developer.getName());
        response.setEmail(developer.getEmail());
        response.setSkillsTechStack(developer.getSkillsTechStack());
        response.setModuleCount(developer.getModules().size());
        if (developer.getUser() != null) {
            response.setUserId(developer.getUser().getId());
            response.setUserUsername(developer.getUser().getUsername());
        }

        // Attach metrics if available
        metricsRepository.findByDeveloperId(developer.getId()).ifPresent(m -> {
            int resolved = m.getBugsResolved();
            int reopened = m.getBugsReopened();
            int workload = workloadService.getCurrentWorkload(developer.getId());

            Double fixRate = null;
            Double avgResHours = null;
            if (resolved > 0) {
                double rate = Math.max(0.0, Math.min(100.0, ((double) (resolved - reopened) / resolved) * 100.0));
                fixRate = Math.round(rate * 10.0) / 10.0;

                double hours = (double) m.getTotalResolutionTimeMinutes() / (60.0 * resolved);
                avgResHours = Math.round(hours * 10.0) / 10.0;
            }

            Double slaCompliance = null;
            int totalSla = m.getSlaMetCount() + m.getSlaBreachedCount();
            if (totalSla > 0) {
                double rate = ((double) m.getSlaMetCount() / totalSla) * 100.0;
                slaCompliance = Math.round(rate * 10.0) / 10.0;
            }

            boolean hasHistory = resolved > 0 || reopened > 0 || m.getTotalBugsHandled() > 0;

            com.bmaas.dto.metrics.DeveloperMetricsResponse metricsRes = com.bmaas.dto.metrics.DeveloperMetricsResponse.builder()
                    .developerId(developer.getId())
                    .developerName(developer.getName())
                    .developerEmail(developer.getEmail())
                    .skillsTechStack(developer.getSkillsTechStack())
                    .userId(developer.getUser() != null ? developer.getUser().getId() : null)
                    .userUsername(developer.getUser() != null ? developer.getUser().getUsername() : null)
                    .totalBugsHandled(m.getTotalBugsHandled())
                    .bugsResolved(resolved)
                    .bugsReopened(reopened)
                    .firstTimeFixRate(fixRate)
                    .averageResolutionTimeHours(avgResHours)
                    .slaMetCount(m.getSlaMetCount())
                    .slaBreachedCount(m.getSlaBreachedCount())
                    .slaComplianceRate(slaCompliance)
                    .currentWorkload(workload)
                    .hasHistory(hasHistory)
                    .build();

            response.setMetrics(metricsRes);
        });

        response.setCreatedAt(developer.getCreatedAt());
        response.setUpdatedAt(developer.getUpdatedAt());
        return response;
    }
}