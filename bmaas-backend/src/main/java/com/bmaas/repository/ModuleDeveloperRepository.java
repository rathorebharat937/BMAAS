package com.bmaas.repository;

import com.bmaas.entity.ModuleDeveloper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleDeveloperRepository extends JpaRepository<ModuleDeveloper, Long> {

    List<ModuleDeveloper> findByModuleId(Long moduleId);

    @Query("SELECT md FROM ModuleDeveloper md JOIN FETCH md.developer d LEFT JOIN FETCH md.module m WHERE md.module.id = :moduleId")
    List<ModuleDeveloper> findByModuleIdWithDeveloper(@Param("moduleId") Long moduleId);

    List<ModuleDeveloper> findByDeveloperId(Long developerId);

    Optional<ModuleDeveloper> findByModuleIdAndDeveloperId(Long moduleId, Long developerId);

    boolean existsByModuleIdAndDeveloperId(Long moduleId, Long developerId);

    void deleteByModuleIdAndDeveloperId(Long moduleId, Long developerId);

    @Query("SELECT COUNT(DISTINCT m.project.id) FROM ModuleDeveloper md JOIN md.module m WHERE md.developer.id = :developerId AND m.project.status = com.bmaas.entity.Project.ProjectStatus.ACTIVE")
    long countActiveProjectsByDeveloperId(@Param("developerId") Long developerId);

    @Query("SELECT COUNT(DISTINCT md.module.id) FROM ModuleDeveloper md WHERE md.developer.id = :developerId AND md.module.status != com.bmaas.entity.Module.ModuleStatus.COMPLETED")
    long countActiveModulesByDeveloperId(@Param("developerId") Long developerId);
}
