package com.studproj.axiom.application.features.workitems.assignworkitem;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignWorkItemCommandHandler {
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(AssignWorkItemCommand command) {
        WorkItem workItem = workItemRepository.findById(command.id())
                .orElseThrow(() -> new EntityNotFoundException("WorkItem not found"));

        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, workItem.getProjectId())) {
            throw new AccessDeniedException("You are not a member of this project");
        }

        if (command.assigneeId() != null && !projectMembershipRepository.existsByProjectIdAndUserId(workItem.getProjectId(), command.assigneeId())) {
            throw new AccessDeniedException("Assignee must be a member of this project");
        }

        workItem.assignTo(command.assigneeId());
        workItemRepository.save(workItem);
    }
}
