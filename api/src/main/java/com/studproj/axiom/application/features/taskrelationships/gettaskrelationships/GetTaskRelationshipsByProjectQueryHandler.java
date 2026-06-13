package com.studproj.axiom.application.features.taskrelationships.gettaskrelationships;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.application.features.taskrelationships.TaskRelationshipDto;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.TaskRelationshipRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetTaskRelationshipsByProjectQueryHandler {
    private final TaskRelationshipRepository taskRelationshipRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<TaskRelationshipDto> handle(GetTaskRelationshipsByProjectQuery query) {
        ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                query.projectId()
        );

        return taskRelationshipRepository.findByProjectId(query.projectId()).stream()
                .map(r -> new TaskRelationshipDto(r.getId(), r.getSourceId(), r.getTargetId(), r.getLinkType()))
                .collect(Collectors.toList());
    }
}
