package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.WorkItemDto;
import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkItemQueryHandler {
    private final WorkItemRepository workItemRepository;
        private final ProjectRepository projectRepository;
        private final ProjectMembershipRepository projectMembershipRepository;
        private final AuthenticatedUserProvider authenticatedUserProvider;

    public List<WorkItemDto> getWorkItemsByProject(UUID projectId) {
        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, projectId)) {
            throw new ForbiddenException("You are not a member of this project");
        }

        return workItemRepository.findByProjectId(projectId).stream()
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

    public Optional<WorkItemDto> getWorkItemById(UUID id) {
        return workItemRepository.findById(id)
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
