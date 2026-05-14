package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkItemRepository {
    void save(WorkItem workItem);

    Optional<WorkItem> findById(UUID id);

    List<WorkItem> findByProjectId(UUID projectId);

    long countByStatusIn(Collection<WorkItemStatus> statuses);

    List<WorkItem> findRecent(int limit);

    Optional<Integer> findMaxControlNoByProjectId(UUID projectId);

    void delete(UUID id);
}
