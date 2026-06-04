package com.studproj.axiom.application.features.projects.updateproject;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProjectCommandHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(UUID projectId, UpdateProjectCommand command) {
        ProjectAccessChecks.ensureProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId);

        Project existing = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        String normalizedCode = command.code().trim().toUpperCase();
        projectRepository.findByCode(normalizedCode)
                .filter(project -> !project.getId().equals(projectId))
                .ifPresent(project -> {
                    throw new BadRequestException("A project with this code already exists");
                });

        Project updated = new Project(
                existing.getId(),
                command.name().trim(),
                normalizedCode,
                existing.getDescription(),
                existing.getCreatedOn(),
                existing.getOwnerId()
        );

        projectRepository.save(updated);
    }
}
