package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateWorkItemCommand;
import com.studproj.axiom.application.dto.command.UpdateWorkItemCommand;
import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkItemCommandHandler {
    private final WorkItemRepository workItemRepository;
    private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final ProjectMembershipRepository projectMembershipRepository;
        private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public UUID createWorkItem(CreateWorkItemCommand command) {
        String email = authenticatedUserProvider.getAuthenticatedUserEmail();
        UUID authorId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();

        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, command.projectId())) {
            throw new ForbiddenException("You are not a member of this project");
        }

        if (command.assigneeId() != null && !projectMembershipRepository.existsByProjectIdAndUserId(command.projectId(), command.assigneeId())) {
            throw new ForbiddenException("Assignee must be a member of this project");
        }

        int nextControlNo = workItemRepository
                .findMaxControlNoByProjectId(command.projectId())
                .map(max -> max + 1)
                .orElse(1);

        WorkItem workItem = WorkItem.builder()
                .id(UUID.randomUUID())
                .controlNo(nextControlNo)
                .description(command.description())
                .priority(command.priority())
                .type(command.type())
                .status(command.status())
                .dueDate(command.dueDate())
                .estimatedEffort(command.estimatedEffort())
                .projectId(command.projectId())
                .authorId(authorId)
                .assigneeId(command.assigneeId())
                .build();

        workItemRepository.save(workItem);
        return workItem.getId();
    }

    @Transactional
    public void assignWorkItem(UUID id, UUID assigneeId) {
        WorkItem workItem = workItemRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, workItem.getProjectId())) {
            throw new ForbiddenException("You are not a member of this project");
        }

        if (assigneeId != null && !projectMembershipRepository.existsByProjectIdAndUserId(workItem.getProjectId(), assigneeId)) {
            throw new ForbiddenException("Assignee must be a member of this project");
        }

        workItem.assignTo(assigneeId);
        workItemRepository.save(workItem);
    }

    @Transactional
    public void updateStatus(UUID id, com.studproj.axiom.domain.model.WorkItemStatus status) {
        WorkItem workItem = workItemRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, workItem.getProjectId())) {
            throw new ForbiddenException("You are not a member of this project");
        }

        workItem.updateStatus(status);
        workItemRepository.save(workItem);
    }

    @Transactional
    public void updateNotes(UUID id, String notes) {
        WorkItem workItem = workItemRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        ensureAuthorOrProjectAdmin(workItem);
        workItem.updateNotes(notes);
        workItemRepository.save(workItem);
    }

    @Transactional
    public void updateWorkItem(UUID id, UpdateWorkItemCommand command) {
        WorkItem workItem = workItemRepository.findById(id)
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
        if (command.notes() != null) {
                workItem.updateNotes(command.notes());
        }
        workItem.assignTo(command.assigneeId());

        workItemRepository.save(workItem);
    }

    @Transactional
    public void deleteWorkItem(UUID id) {
        WorkItem workItem = workItemRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        ensureAuthorOrProjectAdmin(workItem);
        workItemRepository.delete(id);
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

