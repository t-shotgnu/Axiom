package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.infrastructure.persistence.entity.ProjectRoleEntity;

public class ProjectRoleMapper {
    public static ProjectRole toDomain(ProjectRoleEntity entity) {
        if (entity == null) return null;
        return ProjectRole.builder()
                .id(entity.getId())
                .type(ProjectRoleType.valueOf(entity.getCode()))
                .build();
    }

    public static ProjectRoleEntity toEntity(ProjectRole domain) {
        if (domain == null) return null;
        return ProjectRoleEntity.builder()
                .id(domain.getId())
                .code(domain.getType().name())
                .build();
    }
}