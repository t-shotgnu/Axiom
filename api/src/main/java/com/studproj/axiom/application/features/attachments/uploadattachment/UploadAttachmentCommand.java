package com.studproj.axiom.application.features.attachments.uploadattachment;

import java.util.UUID;

public record UploadAttachmentCommand(
        UUID workItemId,
        String fileName,
        String fileType,
        byte[] content
) {
}
