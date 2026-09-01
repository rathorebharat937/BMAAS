package com.bmaas.repository;

import com.bmaas.entity.Developer;
import com.bmaas.entity.DeveloperMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperMetricsRepository extends JpaRepository<DeveloperMetrics, Long> {

    Optional<DeveloperMetrics> findByDeveloperId(Long developerId);

    Optional<DeveloperMetrics> findByDeveloper(Developer developer);

    List<DeveloperMetrics> findByDeveloperIn(List<Developer> developers);
}
