package com.studproj.axiom.application.features.attachments.downloadattachment;

public record DownloadAttachmentResult(
        String fileName,
        String fileType,
        byte[] content
) {
}
