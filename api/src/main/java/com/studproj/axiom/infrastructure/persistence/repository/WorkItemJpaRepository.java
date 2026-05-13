package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.infrastructure.persistence.entity.WorkItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkItemJpaRepository extends JpaRepository<WorkItemEntity, UUID> {
    List<WorkItemEntity> findByProjectId(UUID projectId);

    @Query("SELECT MAX(w.controlNo) FROM WorkItemEntity w WHERE w.projectId = :projectId")
    Optional<Integer> findMaxControlNoByProjectId(@Param("projectId") UUID projectId);
}
