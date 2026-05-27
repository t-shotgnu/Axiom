package com.studproj.axiom.application.handlers;

import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;

import java.util.UUID;

final class ProjectAccessChecks {
    private ProjectAccessChecks() {
    }

    static void ensureProjectExists(ProjectRepository projectRepository, UUID projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new NotFoundException("Project not found");
        }
    }

    static ProjectMembership ensureProjectMember(
            ProjectRepository projectRepository,
            ProjectMembershipRepository projectMembershipRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            UUID projectId
    ) {
        ensureProjectExists(projectRepository, projectId);
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        return projectMembershipRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));
    }

    static boolean isProjectMember(
        ProjectRepository projectRepository,
        ProjectMembershipRepository projectMembershipRepository,
        AuthenticatedUserProvider authenticatedUserProvider,
        UUID projectId
    ) {
        ensureProjectExists(projectRepository, projectId);
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        return projectMembershipRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    static void ensureProjectAdmin(
            ProjectRepository projectRepository,
            ProjectMembershipRepository projectMembershipRepository,
            ProjectRoleRepository projectRoleRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            UUID projectId
    ) {
        ProjectMembership membership = ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                projectId);

        var role = projectRoleRepository.findById(membership.getRoleId())
                .orElseThrow(() -> new NotFoundException("Project role not found"));

        if (role.getType() != ProjectRoleType.ADMIN) {
            throw new ForbiddenException("You are not allowed to manage this project");
        }
    }

    static boolean isProjectAdmin(
            ProjectRepository projectRepository,
            ProjectMembershipRepository projectMembershipRepository,
            ProjectRoleRepository projectRoleRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            UUID projectId
    ) {
        if (!isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, projectId)) {
            return false;
        }

        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        ProjectMembership membership = projectMembershipRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));

        var role = projectRoleRepository.findById(membership.getRoleId())
                .orElseThrow(() -> new NotFoundException("Project role not found"));

        return role.getType() == ProjectRoleType.ADMIN;
    }
}