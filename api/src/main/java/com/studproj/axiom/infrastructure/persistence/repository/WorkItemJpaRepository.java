package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.infrastructure.persistence.entity.WorkItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WorkItemJpaRepository extends JpaRepository<WorkItemEntity, UUID> {
    List<WorkItemEntity> findByProjectId(UUID projectId);
}
