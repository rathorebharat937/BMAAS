package com.bmaas.service;

import com.bmaas.dto.bug.BugRequest;
import com.bmaas.dto.bug.BugResponse;
import com.bmaas.entity.Bug.BugSeverity;
import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.entity.Bug.SlaStatus;
import com.bmaas.entity.User.UserRole;

import java.util.List;

public interface BugService {

    BugResponse createBug(Long projectId, Long moduleId, BugRequest request, Long reporterId);

    List<BugResponse> getBugsByProject(Long projectId, Long moduleId, BugStatus status, BugSeverity severity, SlaStatus slaStatus);

    List<BugResponse> getBugsForDeveloper(Long userId, Long moduleId, BugStatus status, BugSeverity severity, SlaStatus slaStatus);

    BugResponse getBugById(Long bugId);

    BugResponse getBugById(Long bugId, Long currentUserId, UserRole currentUserRole);

    BugResponse updateBug(Long bugId, BugRequest request);

    BugResponse changeStatus(Long bugId, BugStatus newStatus);

    BugResponse changeStatus(Long bugId, BugStatus newStatus, String resolutionNotes, Long currentUserId, UserRole currentUserRole);

    BugResponse assignBug(Long bugId, Long developerId);

    BugResponse assignBug(Long bugId, Long developerId, Long assignedByUserId);

    BugResponse unassignBug(Long bugId);

    void deleteBug(Long bugId);
}
