package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.Comment;
import com.studproj.axiom.infrastructure.persistence.entity.CommentEntity;

public class CommentMapper {
    public static Comment toDomain(CommentEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Comment(
                entity.getId(),
                entity.getWorkItemId(),
                entity.getAuthorId(),
                entity.getText(),
                entity.getCreatedOn());
    }

    public static CommentEntity toEntity(Comment domain) {
        if (domain == null) {
            return null;
        }

        return CommentEntity.builder()
                .id(domain.getId())
                .workItemId(domain.getWorkItemId())
                .authorId(domain.getAuthorId())
                .text(domain.getText())
                .createdOn(domain.getCreatedOn())
                .build();
    }
}