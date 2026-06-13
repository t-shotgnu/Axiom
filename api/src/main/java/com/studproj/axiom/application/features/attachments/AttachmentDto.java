package com.studproj.axiom.application.features.attachments;

import com.studproj.axiom.domain.model.Attachment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentDto(
        UUID id,
        UUID workItemId,
        UUID uploadedBy,
        String fileName,
        Long fileSize,
        String fileType,
        LocalDateTime uploadedOn
) {
    public static AttachmentDto fromDomain(Attachment entity) {
        return new AttachmentDto(
                entity.getId(),
                entity.getWorkItemId(),
                entity.getUploadedBy(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getFileType(),
                entity.getUploadedOn());
    }
}
