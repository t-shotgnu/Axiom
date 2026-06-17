package com.studproj.axiom.application.features.projects;

import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAccessChecksTest {

    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID roleId = UUID.randomUUID();
    private final Project project = new Project(projectId, "Axiom", "AX", "Main project", LocalDateTime.now(), userId);
    private final ProjectMembership membership = new ProjectMembership(
            UUID.randomUUID(),
            projectId,
            userId,
            roleId,
            LocalDateTime.now()
    );

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMembershipRepository projectMembershipRepository;
    @Mock private ProjectRoleRepository projectRoleRepository;
    @Mock private AuthenticatedUserProvider authenticatedUserProvider;

    @Test
    void ensureProjectExistsThrowsWhenProjectIsMissing() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ProjectAccessChecks.ensureProjectExists(projectRepository, projectId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Project not found");
    }

    @Test
    void ensureProjectMemberReturnsMembershipForAuthenticatedMember() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(membership));

        ProjectMembership result = ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                projectId
        );

        assertThat(result).isEqualTo(membership);
    }

    @Test
    void ensureProjectMemberRejectsNonMembers() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ProjectAccessChecks.ensureProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                projectId
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this project");
    }

    @Test
    void isProjectMemberReturnsRepositoryMembershipResult() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(true);

        boolean result = ProjectAccessChecks.isProjectMember(
                projectRepository,
                projectMembershipRepository,
                authenticatedUserProvider,
                projectId
        );

        assertThat(result).isTrue();
        verify(projectMembershipRepository).existsByProjectIdAndUserId(projectId, userId);
    }

    @Test
    void ensureProjectAdminAllowsAdmins() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(membership));
        when(projectRoleRepository.findById(roleId))
                .thenReturn(Optional.of(new ProjectRole(roleId, ProjectRoleType.ADMIN)));

        ProjectAccessChecks.ensureProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId
        );

        verify(projectRoleRepository).findById(roleId);
    }

    @Test
    void ensureProjectAdminRejectsMembersAndMissingRoles() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(membership));
        when(projectRoleRepository.findById(roleId))
                .thenReturn(Optional.of(new ProjectRole(roleId, ProjectRoleType.MEMBER)));

        assertThatThrownBy(() -> ProjectAccessChecks.ensureProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not allowed to manage this project");

        when(projectRoleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ProjectAccessChecks.ensureProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Project role not found");
    }

    @Test
    void isProjectAdminReturnsFalseForNonMembersAndTrueForAdmins() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(false);

        boolean notMemberResult = ProjectAccessChecks.isProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId
        );

        assertThat(notMemberResult).isFalse();

        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(true);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(membership));
        when(projectRoleRepository.findById(roleId))
                .thenReturn(Optional.of(new ProjectRole(roleId, ProjectRoleType.ADMIN)));

        boolean adminResult = ProjectAccessChecks.isProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId
        );

        assertThat(adminResult).isTrue();
    }
}
