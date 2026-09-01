package com.bmaas.service;

import com.bmaas.dto.metrics.AssignmentHistoryResponse;
import com.bmaas.dto.metrics.DeveloperLeaderboardItemResponse;
import com.bmaas.dto.metrics.DeveloperMetricsResponse;
import com.bmaas.entity.Bug;
import com.bmaas.entity.Developer;
import com.bmaas.entity.User;

import java.util.List;

public interface DeveloperMetricsService {

    void recordBugAssigned(Developer developer, Bug bug, User assignedBy);

    void recordBugUnassigned(Bug bug);

    void recordBugClosed(Bug bug);

    void recordBugReopened(Bug bug);

    DeveloperMetricsResponse getMetricsForDeveloper(Long developerId);

    List<DeveloperLeaderboardItemResponse> getLeaderboard(String sortBy, String sortOrder);

    List<AssignmentHistoryResponse> getAssignmentHistoryForBug(Long bugId);

    List<AssignmentHistoryResponse> getAssignmentHistoryForDeveloper(Long developerId);
}
