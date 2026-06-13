package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.LinkType;
import com.studproj.axiom.infrastructure.persistence.entity.TaskRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRelationshipJpaRepository extends JpaRepository<TaskRelationshipEntity, UUID> {
    
    @Query("SELECT tr FROM TaskRelationshipEntity tr WHERE tr.sourceId IN (SELECT w.id FROM WorkItemEntity w WHERE w.projectId = :projectId)")
    List<TaskRelationshipEntity> findByProjectId(@Param("projectId") UUID projectId);

    List<TaskRelationshipEntity> findBySourceId(UUID sourceId);

    List<TaskRelationshipEntity> findByTargetId(UUID targetId);

    List<TaskRelationshipEntity> findBySourceIdAndLinkType(UUID sourceId, LinkType linkType);

    List<TaskRelationshipEntity> findByTargetIdAndLinkType(UUID targetId, LinkType linkType);

    Optional<TaskRelationshipEntity> findBySourceIdAndTargetIdAndLinkType(UUID sourceId, UUID targetId, LinkType linkType);
}
