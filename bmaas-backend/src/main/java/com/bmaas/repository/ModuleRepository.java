package com.bmaas.repository;

import com.bmaas.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByProjectId(Long projectId);

    List<Module> findByProjectIdOrderByPriorityDesc(Long projectId);
}
