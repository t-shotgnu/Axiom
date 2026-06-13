package com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.ProjectMembership;
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
public class ChangeProjectMemberRoleCommandHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(UUID projectId, UUID userId, ChangeProjectMemberRoleCommand command) {
        ProjectAccessChecks.ensureProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId);

        if (authenticatedUserProvider.getAuthenticatedUserId().equals(userId)) {
            throw new AccessDeniedException("You cannot change your own role");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (project.getOwnerId() != null && project.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Project lead role cannot be changed");
        }

        ProjectMembership membership = projectMembershipRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Project member not found"));

        var role = projectRoleRepository.findByType(command.role())
                .orElseThrow(() -> new EntityNotFoundException("Project role not found"));

        membership.changeRole(role.getId());
        projectMembershipRepository.save(membership);
    }
}
