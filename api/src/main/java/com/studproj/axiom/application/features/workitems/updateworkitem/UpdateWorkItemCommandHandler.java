package com.studproj.axiom.application.features.workitems.updateworkitem;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateWorkItemCommandHandler {
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(UpdateWorkItemCommand command) {
        WorkItem workItem = workItemRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        ensureAuthorOrProjectAdmin(workItem);

        if (command.assigneeId() != null && !projectMembershipRepository.existsByProjectIdAndUserId(workItem.getProjectId(), command.assigneeId())) {
            throw new ForbiddenException("Assignee must be a member of this project");
        }

        workItem.updateDetails(
                command.description(),
                command.priority(),
                command.type(),
                command.dueDate(),
                command.estimatedEffort());
        if (command.status() != null) {
            workItem.updateStatus(command.status());
        }
        if (command.notes() != null) {
            workItem.updateNotes(command.notes());
        }
        workItem.assignTo(command.assigneeId());

        workItemRepository.save(workItem);
    }

    private void ensureAuthorOrProjectAdmin(WorkItem workItem) {
        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, workItem.getProjectId())) {
            throw new ForbiddenException("You are not a member of this project");
        }

        UUID currentUserId = authenticatedUserProvider.getAuthenticatedUserId();
        if (workItem.getAuthorId() != null && workItem.getAuthorId().equals(currentUserId)) {
            return;
        }

        if (ProjectAccessChecks.isProjectAdmin(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider, workItem.getProjectId())) {
            return;
        }

        throw new ForbiddenException("Only the author or project admin can modify this work item");
    }
}
