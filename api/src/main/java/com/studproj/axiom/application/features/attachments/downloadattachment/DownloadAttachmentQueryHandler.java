package com.studproj.axiom.application.features.attachments.downloadattachment;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DownloadAttachmentQueryHandler {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentObjectRepository attachmentObjectRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DownloadAttachmentResult handle(DownloadAttachmentQuery query) {
        Attachment attachment = attachmentRepository.findById(query.id())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));

        WorkItem workItem = workItemRepository.findById(attachment.getWorkItemId())
                .orElseThrow(() -> new EntityNotFoundException("WorkItem not found"));

        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                workItem.getProjectId());

        return new DownloadAttachmentResult(
                attachment.getFileName(),
                attachment.getFileType(),
                attachmentObjectRepository.findByObjectKey(attachment.getObjectKey()));
    }
}
