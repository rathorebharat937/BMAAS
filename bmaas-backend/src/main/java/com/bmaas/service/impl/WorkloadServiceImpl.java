package com.bmaas.service.impl;

import com.bmaas.entity.Bug.BugStatus;
import com.bmaas.repository.BugRepository;
import com.bmaas.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final BugRepository bugRepository;

    @Override
    @Transactional(readOnly = true)
    public int getCurrentWorkload(Long developerId) {
        if (developerId == null) {
            return 0;
        }
        return (int) bugRepository.countByAssignedDeveloperIdAndStatusIn(
                developerId,
                List.of(BugStatus.ASSIGNED, BugStatus.IN_PROGRESS)
        );
    }
}
