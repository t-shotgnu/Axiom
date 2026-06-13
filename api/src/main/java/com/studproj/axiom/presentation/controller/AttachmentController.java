package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.attachments.AttachmentDto;
import com.studproj.axiom.application.features.attachments.deleteattachment.DeleteAttachmentCommand;
import com.studproj.axiom.application.features.attachments.deleteattachment.DeleteAttachmentCommandHandler;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentQuery;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentQueryHandler;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentResult;
import com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQuery;
import com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQueryHandler;
import com.studproj.axiom.application.features.attachments.uploadattachment.UploadAttachmentCommand;
import com.studproj.axiom.application.features.attachments.uploadattachment.UploadAttachmentCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {
    private final GetAttachmentsByWorkItemQueryHandler getAttachmentsByWorkItemQueryHandler;
    private final UploadAttachmentCommandHandler uploadAttachmentCommandHandler;
    private final DownloadAttachmentQueryHandler downloadAttachmentQueryHandler;
    private final DeleteAttachmentCommandHandler deleteAttachmentCommandHandler;

    @GetMapping("/work-items/{workItemId}/attachments")
    public ResponseEntity<List<AttachmentDto>> getAttachments(@PathVariable UUID workItemId) {
        return ResponseEntity.ok(getAttachmentsByWorkItemQueryHandler.handle(new GetAttachmentsByWorkItemQuery(workItemId)));
    }

    @PostMapping(value = "/work-items/{workItemId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UUID> uploadAttachment(@PathVariable UUID workItemId, @RequestPart("file") MultipartFile file) throws IOException {
        UUID id = uploadAttachmentCommandHandler.handle(new UploadAttachmentCommand(
                workItemId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()));

        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable UUID id) {
        DownloadAttachmentResult result = downloadAttachmentQueryHandler.handle(new DownloadAttachmentQuery(id));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(result.fileName())
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(result.fileType()))
                .body(result.content());
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID id) {
        deleteAttachmentCommandHandler.handle(new DeleteAttachmentCommand(id));
        return ResponseEntity.noContent().build();
    }
}
