package com.bmaas.repository;

import com.bmaas.entity.Bug;
import com.bmaas.entity.Bug.BugSeverity;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.Bug.SlaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    List<Bug> findByProjectId(Long projectId);

    List<Bug> findByProjectIdAndModuleId(Long projectId, Long moduleId);

    List<Bug> findByProjectIdAndStatus(Long projectId, BugStatus status);

    List<Bug> findByProjectIdAndSeverity(Long projectId, BugSeverity severity);

    List<Bug> findByProjectIdAndSlaStatus(Long projectId, SlaStatus slaStatus);

    List<Bug> findByReporterId(Long reporterId);

    @Query("SELECT b FROM Bug b WHERE b.project.id = :projectId" +
           " AND (:moduleId IS NULL OR b.module.id = :moduleId)" +
           " AND (:status IS NULL OR b.status = :status)" +
           " AND (:severity IS NULL OR b.severity = :severity)" +
           " AND (:slaStatus IS NULL OR b.slaStatus = :slaStatus)" +
           " ORDER BY b.createdAt DESC")
    List<Bug> findByProjectWithFilters(
            @Param("projectId") Long projectId,
            @Param("moduleId") Long moduleId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("slaStatus") SlaStatus slaStatus);

    @Query("SELECT b FROM Bug b WHERE b.assignedDeveloper.id = :developerId" +
           " AND (:moduleId IS NULL OR b.module.id = :moduleId)" +
           " AND (:status IS NULL OR b.status = :status)" +
           " AND (:severity IS NULL OR b.severity = :severity)" +
           " AND (:slaStatus IS NULL OR b.slaStatus = :slaStatus)" +
           " ORDER BY b.createdAt DESC")
    List<Bug> findByAssignedDeveloperWithFilters(
            @Param("developerId") Long developerId,
            @Param("moduleId") Long moduleId,
            @Param("status") BugStatus status,
            @Param("severity") BugSeverity severity,
            @Param("slaStatus") SlaStatus slaStatus);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, BugStatus status);

    long countByProjectIdAndSlaStatus(Long projectId, SlaStatus slaStatus);

    long countByAssignedDeveloperIdAndStatusIn(Long developerId, java.util.Collection<BugStatus> statuses);
}
