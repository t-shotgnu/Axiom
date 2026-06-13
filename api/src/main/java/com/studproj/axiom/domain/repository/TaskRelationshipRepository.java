package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.LinkType;
import com.studproj.axiom.domain.model.TaskRelationship;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRelationshipRepository {
    void save(TaskRelationship relationship);

    void delete(UUID id);

    Optional<TaskRelationship> findById(UUID id);

    List<TaskRelationship> findByProjectId(UUID projectId);

    List<TaskRelationship> findBySourceId(UUID sourceId);

    List<TaskRelationship> findByTargetId(UUID targetId);

    List<TaskRelationship> findBySourceIdAndLinkType(UUID sourceId, LinkType linkType);

    List<TaskRelationship> findByTargetIdAndLinkType(UUID targetId, LinkType linkType);

    Optional<TaskRelationship> findBySourceIdAndTargetIdAndLinkType(UUID sourceId, UUID targetId, LinkType linkType);
}
