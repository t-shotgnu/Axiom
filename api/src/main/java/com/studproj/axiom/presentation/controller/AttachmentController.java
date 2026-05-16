package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.infrastructure.persistence.entity.AttachmentEntity;
import com.studproj.axiom.infrastructure.persistence.repository.AttachmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentJpaRepository attachmentRepository;

    @GetMapping("/work-items/{workItemId}/attachments")
    public ResponseEntity<List<AttachmentEntity>> getAttachments(@PathVariable UUID workItemId) {
        List<AttachmentEntity> list = attachmentRepository.findByWorkItemId(workItemId);
        if (list.isEmpty()) {
            AttachmentEntity file1 = AttachmentEntity.builder()
                    .id(UUID.randomUUID())
                    .workItemId(workItemId)
                    .fileName("mockup_desktop.png")
                    .fileSize("2.4 MB")
                    .fileType("image")
                    .uploadedOn(LocalDateTime.now().minusHours(2))
                    .build();
            AttachmentEntity file2 = AttachmentEntity.builder()
                    .id(UUID.randomUUID())
                    .workItemId(workItemId)
                    .fileName("grid_specs.pdf")
                    .fileSize("1.1 MB")
                    .fileType("pdf")
                    .uploadedOn(LocalDateTime.now().minusHours(1))
                    .build();
            attachmentRepository.save(file1);
            attachmentRepository.save(file2);
            list = List.of(file1, file2);
        }
        return ResponseEntity.ok(list);
    }
}
