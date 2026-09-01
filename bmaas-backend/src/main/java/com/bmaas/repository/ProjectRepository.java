package com.bmaas.repository;

import com.bmaas.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByProjectManagerId(Long projectManagerId);

    Optional<Project> findByIdAndProjectManagerId(Long id, Long projectManagerId);
}
