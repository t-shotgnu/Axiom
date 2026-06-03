package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.infrastructure.persistence.entity.ProjectEntity;

public class ProjectMapper {
    public static Project toDomain(ProjectEntity entity) {
        if (entity == null) return null;
        return new Project(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.getCreatedOn(),
                entity.getOwnerId()
        );
    }

    public static ProjectEntity toEntity(Project domain) {
        if (domain == null) return null;
        return ProjectEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .code(domain.getCode())
                .description(domain.getDescription())
                .createdOn(domain.getCreatedOn())
                .ownerId(domain.getOwnerId())
                .build();
    }
}
