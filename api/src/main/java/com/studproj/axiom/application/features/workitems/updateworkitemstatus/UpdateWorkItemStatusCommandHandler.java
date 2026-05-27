package com.studproj.axiom.application.features.workitems.updateworkitemstatus;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.exception.NotFoundException;
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
public class UpdateWorkItemStatusCommandHandler {
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(UpdateWorkItemStatusCommand command) {
        WorkItem workItem = workItemRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, workItem.getProjectId())) {
            throw new ForbiddenException("You are not a member of this project");
        }

        workItem.updateStatus(command.status());
        workItemRepository.save(workItem);
    }
}
