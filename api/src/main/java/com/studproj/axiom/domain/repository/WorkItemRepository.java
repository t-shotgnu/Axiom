package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.WorkItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkItemRepository {
    void save(WorkItem workItem);

    Optional<WorkItem> findById(UUID id);

    List<WorkItem> findByProjectId(UUID projectId);

    void delete(UUID id);
}
