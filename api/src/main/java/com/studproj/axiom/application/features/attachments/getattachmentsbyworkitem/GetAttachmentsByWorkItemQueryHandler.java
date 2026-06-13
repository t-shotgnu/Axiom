package com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem;

import com.studproj.axiom.application.features.attachments.AttachmentDto;
import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.AttachmentRepository;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAttachmentsByWorkItemQueryHandler {
    private final AttachmentRepository attachmentRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public List<AttachmentDto> handle(GetAttachmentsByWorkItemQuery query) {
        WorkItem workItem = workItemRepository.findById(query.workItemId())
                .orElseThrow(() -> new EntityNotFoundException("WorkItem not found"));

        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                workItem.getProjectId());

        return attachmentRepository.findByWorkItemIdOrderByUploadedOnDesc(query.workItemId()).stream()
                .map(AttachmentDto::fromDomain)
                .toList();
    }
}
