package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.TaskRelationship;
import com.studproj.axiom.infrastructure.persistence.entity.TaskRelationshipEntity;

public class TaskRelationshipMapper {
    public static TaskRelationship toDomain(TaskRelationshipEntity entity) {
        if (entity == null) return null;
        return TaskRelationship.builder()
                .id(entity.getId())
                .sourceId(entity.getSourceId())
                .targetId(entity.getTargetId())
                .linkType(entity.getLinkType())
                .build();
    }

    public static TaskRelationshipEntity toEntity(TaskRelationship domain) {
        if (domain == null) return null;
        return TaskRelationshipEntity.builder()
                .id(domain.getId())
                .sourceId(domain.getSourceId())
                .targetId(domain.getTargetId())
                .linkType(domain.getLinkType())
                .build();
    }
}
