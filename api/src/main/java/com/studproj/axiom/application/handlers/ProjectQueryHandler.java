package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.ProjectDto;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
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
public class ProjectQueryHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public List<ProjectDto> getAllProjects() {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        List<UUID> projectIds = projectMembershipRepository.findByUserId(userId).stream()
                .map(ProjectMembership::getProjectId)
                .toList();

        return projectRepository.findByIds(projectIds).stream()
                .map(project -> new ProjectDto(
                        project.getId(),
                        project.getName(),
                        project.getCode(),
                        project.getDescription(),
                        project.getCreatedOn(),
                        project.getOwnerId()))
                .toList();
    }

    public Optional<ProjectDto> getProjectById(UUID id) {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        boolean isMember = projectMembershipRepository.existsByProjectIdAndUserId(id, userId);

        return isMember ? projectRepository.findById(id)
                .map(project -> new ProjectDto(
                        project.getId(),
                        project.getName(),
                        project.getCode(),
                        project.getDescription(),
                        project.getCreatedOn(),
                        project.getOwnerId())) : Optional.empty();
    }
}
