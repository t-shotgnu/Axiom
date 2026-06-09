package com.studproj.axiom.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Attachment {
    private final UUID id;
    private final UUID workItemId;
    private final UUID uploadedBy;
    private final String objectKey;
    private final String fileName;
    private final Long fileSize;
    private final String fileType;
    private final LocalDateTime uploadedOn;

    public Attachment(
            UUID id,
            UUID workItemId,
            UUID uploadedBy,
            String objectKey,
            String fileName,
            Long fileSize,
            String fileType,
            LocalDateTime uploadedOn
    ) {
        this.id = id;
        this.workItemId = workItemId;
        this.uploadedBy = uploadedBy;
        this.objectKey = objectKey;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.uploadedOn = uploadedOn;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkItemId() {
        return workItemId;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public LocalDateTime getUploadedOn() {
        return uploadedOn;
    }
}
