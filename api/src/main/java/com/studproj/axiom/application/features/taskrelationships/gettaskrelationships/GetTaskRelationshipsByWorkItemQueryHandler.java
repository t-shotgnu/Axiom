package com.studproj.axiom.application.features.taskrelationships.gettaskrelationships;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.application.features.taskrelationships.TaskRelationshipDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetTaskRelationshipsByWorkItemQueryHandler {
    private final TaskRelationshipRepository taskRelationshipRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<TaskRelationshipDto> handle(GetTaskRelationshipsByWorkItemQuery query) {
        WorkItem workItem = workItemRepository.findById(query.workItemId())
                .orElseThrow(() -> new EntityNotFoundException("Work item not found"));

        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                workItem.getProjectId()
        );

        List<TaskRelationship> sourceLinks = taskRelationshipRepository.findBySourceId(query.workItemId());
        List<TaskRelationship> targetLinks = taskRelationshipRepository.findByTargetId(query.workItemId());

        List<TaskRelationship> allLinks = new ArrayList<>();
        allLinks.addAll(sourceLinks);
        for (TaskRelationship targetLink : targetLinks) {
            if (allLinks.stream().noneMatch(l -> l.getId().equals(targetLink.getId()))) {
                allLinks.add(targetLink);
            }
        }

        return allLinks.stream()
                .map(r -> new TaskRelationshipDto(r.getId(), r.getSourceId(), r.getTargetId(), r.getLinkType()))
                .collect(Collectors.toList());
    }
}
