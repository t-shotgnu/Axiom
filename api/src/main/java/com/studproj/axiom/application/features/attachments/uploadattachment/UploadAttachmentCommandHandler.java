package com.studproj.axiom.application.features.attachments.uploadattachment;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.Attachment;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.AttachmentObjectRepository;
import com.studproj.axiom.domain.repository.AttachmentRepository;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadAttachmentCommandHandler {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentObjectRepository attachmentObjectRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public UUID handle(UploadAttachmentCommand command) {
        if (command.content() == null || command.content().length == 0) {
            throw new DomainRuleViolationException("File can not be empty");
        }

        WorkItem workItem = workItemRepository.findById(command.workItemId())
                .orElseThrow(() -> new EntityNotFoundException("WorkItem not found"));

        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                workItem.getProjectId());

        UUID attachmentId = UUID.randomUUID();
        String safeFileName = normalizeFileName(command.fileName());
        String objectKey = workItem.getProjectId() + "/" + command.workItemId() + "/" + attachmentId + "-" + safeFileName;
        String fileType = command.fileType() == null || command.fileType().isBlank()
                ? "application/octet-stream"
                : command.fileType();

        attachmentObjectRepository.save(objectKey, command.content(), fileType);

        Attachment attachment = new Attachment(
                attachmentId,
                command.workItemId(),
                authenticatedUserProvider.getAuthenticatedUserId(),
                objectKey,
                safeFileName,
                (long) command.content().length,
                fileType,
                LocalDateTime.now());

        attachmentRepository.save(attachment);
        return attachmentId;
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment";
        }

        return fileName.replace("\\", "_").replace("/", "_");
    }
}
