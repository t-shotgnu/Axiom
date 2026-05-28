package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.infrastructure.persistence.entity.AttachmentEntity;
import com.studproj.axiom.infrastructure.persistence.repository.AttachmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentJpaRepository attachmentRepository;

    @GetMapping("/work-items/{workItemId}/attachments")
    public ResponseEntity<List<AttachmentEntity>> getAttachments(@PathVariable UUID workItemId) {
        return ResponseEntity.ok(attachmentRepository.findByWorkItemId(workItemId));
    }
}
