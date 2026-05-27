package com.studproj.axiom.application.features.workitems.getworkitemsbyproject;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.application.features.workitems.WorkItemDto;
import com.studproj.axiom.domain.exception.ForbiddenException;
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
public class GetWorkItemsByProjectQueryHandler {
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public List<WorkItemDto> handle(GetWorkItemsByProjectQuery query) {
        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, query.projectId())) {
            throw new ForbiddenException("You are not a member of this project");
        }

        return workItemRepository.findByProjectId(query.projectId()).stream()
                .map(workItem -> new WorkItemDto(
                        workItem.getId(),
                        workItem.getControlNo(),
                        workItem.getDescription(),
                        workItem.getPriority(),
                        workItem.getType(),
                        workItem.getStatus(),
                        workItem.getDueDate(),
                        workItem.getEstimatedEffort(),
                        workItem.getProjectId(),
                        workItem.getAuthorId(),
                        workItem.getAssigneeId(),
                        workItem.getNotes()))
                .toList();
    }
}
