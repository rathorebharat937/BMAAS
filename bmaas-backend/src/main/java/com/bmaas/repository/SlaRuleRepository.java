package com.bmaas.repository;

import com.bmaas.entity.Bug.BugPriority;
import com.bmaas.entity.SlaRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaRuleRepository extends JpaRepository<SlaRule, Long> {

    Optional<SlaRule> findByPriority(BugPriority priority);

    boolean existsByPriority(BugPriority priority);
}
