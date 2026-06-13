package com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.TaskRelationship;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.TaskRelationshipRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTaskRelationshipCommandHandler {
    private final TaskRelationshipRepository taskRelationshipRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(DeleteTaskRelationshipCommand command) {
        TaskRelationship relationship = taskRelationshipRepository.findById(command.id())
                .orElseThrow(() -> new EntityNotFoundException("Task relationship not found"));

        WorkItem sourceTask = workItemRepository.findById(relationship.getSourceId())
                .orElseThrow(() -> new EntityNotFoundException("Source task not found"));

        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                sourceTask.getProjectId()
        );

        taskRelationshipRepository.delete(command.id());
    }
}
