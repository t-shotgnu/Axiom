package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.infrastructure.persistence.entity.WorkItemEntity;

public class WorkItemMapper {
    public static WorkItem toDomain(WorkItemEntity entity) {
        if (entity == null) return null;
        return WorkItem.builder()
                .id(entity.getId())
                .controlNo(entity.getControlNo())
                .description(entity.getDescription())
                .priority(entity.getPriority())
                .type(entity.getType())
                .status(entity.getStatus())
                .dueDate(entity.getDueDate())
                .estimatedEffort(entity.getEstimatedEffort())
                .projectId(entity.getProjectId())
                .authorId(entity.getAuthorId())
                .assigneeId(entity.getAssigneeId())
                .notes(entity.getNotes())
                .build();
    }

    public static WorkItemEntity toEntity(WorkItem domain) {
        if (domain == null) return null;
        return WorkItemEntity.builder()
                .id(domain.getId())
                .controlNo(domain.getControlNo())
                .description(domain.getDescription())
                .priority(domain.getPriority())
                .type(domain.getType())
                .status(domain.getStatus())
                .dueDate(domain.getDueDate())
                .estimatedEffort(domain.getEstimatedEffort())
                .projectId(domain.getProjectId())
                .authorId(domain.getAuthorId())
                .assigneeId(domain.getAssigneeId())
                .notes(domain.getNotes())
                .build();
    }
}
