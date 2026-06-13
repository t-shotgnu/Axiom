package com.studproj.axiom.application.features.workitems.createworkitem;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateWorkItemCommandHandler {
    private final WorkItemRepository workItemRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public UUID handle(CreateWorkItemCommand command) {
        String email = authenticatedUserProvider.getAuthenticatedUserEmail();
        UUID authorId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();

        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, command.projectId())) {
            throw new AccessDeniedException("You are not a member of this project");
        }

        if (command.assigneeId() != null && !projectMembershipRepository.existsByProjectIdAndUserId(command.projectId(), command.assigneeId())) {
            throw new AccessDeniedException("Assignee must be a member of this project");
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
}
