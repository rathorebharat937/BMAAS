package com.bmaas.repository;

import com.bmaas.entity.ModuleDeveloper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleDeveloperRepository extends JpaRepository<ModuleDeveloper, Long> {

    List<ModuleDeveloper> findByModuleId(Long moduleId);

    Optional<ModuleDeveloper> findByModuleIdAndDeveloperId(Long moduleId, Long developerId);

    boolean existsByModuleIdAndDeveloperId(Long moduleId, Long developerId);

    void deleteByModuleIdAndDeveloperId(Long moduleId, Long developerId);
}
