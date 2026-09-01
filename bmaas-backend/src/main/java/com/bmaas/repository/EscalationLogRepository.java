package com.bmaas.repository;

import com.bmaas.entity.EscalationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationLogRepository extends JpaRepository<EscalationLog, Long> {

    List<EscalationLog> findByBugId(Long bugId);

    boolean existsByBugId(Long bugId);
}
