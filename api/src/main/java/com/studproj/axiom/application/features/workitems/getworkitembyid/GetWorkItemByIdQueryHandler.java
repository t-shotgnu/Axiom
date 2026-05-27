package com.studproj.axiom.application.features.workitems.getworkitembyid;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.application.features.workitems.WorkItemDto;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkItemByIdQueryHandler {
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public Optional<WorkItemDto> handle(GetWorkItemByIdQuery query) {
        return workItemRepository.findById(query.id())
                .filter(workItem -> ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, workItem.getProjectId()))
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
                        workItem.getNotes()));
    }
}
