package com.studproj.axiom.application.features.projectmembers.removeprojectmember;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveProjectMemberCommandHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(RemoveProjectMemberCommand command) {
        ProjectAccessChecks.ensureProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                command.projectId());

        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (project.getOwnerId() != null && project.getOwnerId().equals(command.userId())) {
            throw new AccessDeniedException("Project lead cannot be removed from the project");
        }

        if (authenticatedUserProvider.getAuthenticatedUserId().equals(command.userId())) {
            throw new AccessDeniedException("You cannot remove yourself from the project");
        }

        if (projectMembershipRepository.findByProjectIdAndUserId(command.projectId(), command.userId()).isEmpty()) {
            throw new EntityNotFoundException("Project member not found");
        }

        projectMembershipRepository.deleteByProjectIdAndUserId(command.projectId(), command.userId());
    }
}
