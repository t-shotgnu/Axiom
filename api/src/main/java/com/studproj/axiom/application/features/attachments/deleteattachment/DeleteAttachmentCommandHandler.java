package com.studproj.axiom.application.features.attachments.deleteattachment;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.Attachment;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.AttachmentObjectRepository;
import com.studproj.axiom.domain.repository.AttachmentRepository;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAttachmentCommandHandler {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentObjectRepository attachmentObjectRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(DeleteAttachmentCommand command) {
        Attachment attachment = attachmentRepository.findById(command.id())
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));

        WorkItem workItem = workItemRepository.findById(attachment.getWorkItemId())
                .orElseThrow(() -> new EntityNotFoundException("WorkItem not found"));

        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                workItem.getProjectId());

        boolean isUploader = attachment.getUploadedBy().equals(authenticatedUserProvider.getAuthenticatedUserId());
        boolean isAdmin = ProjectAccessChecks.isProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                workItem.getProjectId());

        if (!isUploader && !isAdmin) {
            throw new AccessDeniedException("Only the uploader or project admin can delete this attachment");
        }

        attachmentObjectRepository.delete(attachment.getObjectKey());
        attachmentRepository.delete(command.id());
    }
}
