package com.bmaas.repository;

import com.bmaas.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {

    List<AssignmentHistory> findByBugIdOrderByAssignedAtDesc(Long bugId);

    List<AssignmentHistory> findByDeveloperIdOrderByAssignedAtDesc(Long developerId);

    Optional<AssignmentHistory> findFirstByBugIdAndUnassignedAtIsNullOrderByAssignedAtDesc(Long bugId);
}
