package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.infrastructure.persistence.entity.ProjectMembershipEntity;

public class ProjectMembershipMapper {
    public static ProjectMembership toDomain(ProjectMembershipEntity entity) {
        if (entity == null) return null;
        return new ProjectMembership(
                entity.getId(),
                entity.getProjectId(),
                entity.getUserId(),
                entity.getRoleId(),
                entity.getCreatedOn()
        );
    }

    public static ProjectMembershipEntity toEntity(ProjectMembership domain) {
        if (domain == null) return null;
        return ProjectMembershipEntity.builder()
                .id(domain.getId())
                .projectId(domain.getProjectId())
                .userId(domain.getUserId())
                .roleId(domain.getRoleId())
                .createdOn(domain.getCreatedOn())
                .build();
    }
}