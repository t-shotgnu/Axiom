package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.Attachment;
import com.studproj.axiom.infrastructure.persistence.entity.AttachmentEntity;

public class AttachmentMapper {
    public static Attachment toDomain(AttachmentEntity entity) {
        return new Attachment(
                entity.getId(),
                entity.getWorkItemId(),
                entity.getUploadedBy(),
                entity.getObjectKey(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getFileType(),
                entity.getUploadedOn());
    }

    public static AttachmentEntity toEntity(Attachment domain) {
        return AttachmentEntity.builder()
                .id(domain.getId())
                .workItemId(domain.getWorkItemId())
                .uploadedBy(domain.getUploadedBy())
                .objectKey(domain.getObjectKey())
                .fileName(domain.getFileName())
                .fileSize(domain.getFileSize())
                .fileType(domain.getFileType())
                .uploadedOn(domain.getUploadedOn())
                .build();
    }
}
