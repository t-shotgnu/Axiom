package com.studproj.axiom.application.handlers;

import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.application.dto.command.ChangeProjectMemberRoleCommand;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectMemberCommandHandlerTest {

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMembershipRepository projectMembershipRepository = mock(ProjectMembershipRepository.class);
    private final ProjectRoleRepository projectRoleRepository = mock(ProjectRoleRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthenticatedUserProvider authenticatedUserProvider = mock(AuthenticatedUserProvider.class);
    private final ProjectMemberCommandHandler handler = new ProjectMemberCommandHandler(
            projectRepository,
            projectMembershipRepository,
            projectRoleRepository,
            userRepository,
            authenticatedUserProvider);

    @Test
    void removeMemberRejectsRemovingSelfFromProject() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder().id(projectId).build()));
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId)).thenReturn(Optional.of(
                ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .projectId(projectId)
                        .userId(userId)
                        .roleId(roleId)
                        .createdOn(LocalDateTime.now())
                        .build()));
        when(projectRoleRepository.findById(roleId)).thenReturn(Optional.of(
                ProjectRole.builder().id(roleId).type(ProjectRoleType.ADMIN).build()));

        assertThatThrownBy(() -> handler.removeMember(projectId, userId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You cannot remove yourself from the project");

        verify(projectMembershipRepository, never()).deleteByProjectIdAndUserId(projectId, userId);
    }

    @Test
    void removeMemberDeletesAnotherProjectMember() {
        UUID projectId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID memberUserId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(currentUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder().id(projectId).build()));
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, currentUserId)).thenReturn(Optional.of(
                ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .projectId(projectId)
                        .userId(currentUserId)
                        .roleId(roleId)
                        .createdOn(LocalDateTime.now())
                        .build()));
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, memberUserId)).thenReturn(Optional.of(
                ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .projectId(projectId)
                        .userId(memberUserId)
                        .roleId(UUID.randomUUID())
                        .createdOn(LocalDateTime.now())
                        .build()));
        when(projectRoleRepository.findById(roleId)).thenReturn(Optional.of(
                ProjectRole.builder().id(roleId).type(ProjectRoleType.ADMIN).build()));

        handler.removeMember(projectId, memberUserId);

        verify(projectMembershipRepository).deleteByProjectIdAndUserId(projectId, memberUserId);
    }

    @Test
    void removeMemberRejectsRemovingProjectLead() {
        UUID projectId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID leadUserId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(currentUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder()
                .id(projectId)
                .ownerId(leadUserId)
                .build()));
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, currentUserId)).thenReturn(Optional.of(
                ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .projectId(projectId)
                        .userId(currentUserId)
                        .roleId(roleId)
                        .createdOn(LocalDateTime.now())
                        .build()));
        when(projectRoleRepository.findById(roleId)).thenReturn(Optional.of(
                ProjectRole.builder().id(roleId).type(ProjectRoleType.ADMIN).build()));

        assertThatThrownBy(() -> handler.removeMember(projectId, leadUserId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Project lead cannot be removed from the project");

        verify(projectMembershipRepository, never()).deleteByProjectIdAndUserId(projectId, leadUserId);
    }

    @Test
    void changeRoleRejectsChangingProjectLeadRole() {
        UUID projectId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID leadUserId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(currentUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder()
                .id(projectId)
                .ownerId(leadUserId)
                .build()));
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, currentUserId)).thenReturn(Optional.of(
                ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .projectId(projectId)
                        .userId(currentUserId)
                        .roleId(roleId)
                        .createdOn(LocalDateTime.now())
                        .build()));
        when(projectRoleRepository.findById(roleId)).thenReturn(Optional.of(
                ProjectRole.builder().id(roleId).type(ProjectRoleType.ADMIN).build()));

        assertThatThrownBy(() -> handler.changeRole(projectId, leadUserId, new ChangeProjectMemberRoleCommand("MEMBER")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Project lead role cannot be changed");

        verify(projectMembershipRepository, never()).save(org.mockito.Mockito.any());
    }

    @Test
    void changeRoleRejectsChangingOwnRole() {
        UUID projectId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(currentUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder()
                .id(projectId)
                .build()));
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, currentUserId)).thenReturn(Optional.of(
                ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .projectId(projectId)
                        .userId(currentUserId)
                        .roleId(roleId)
                        .createdOn(LocalDateTime.now())
                        .build()));
        when(projectRoleRepository.findById(roleId)).thenReturn(Optional.of(
                ProjectRole.builder().id(roleId).type(ProjectRoleType.ADMIN).build()));

        assertThatThrownBy(() -> handler.changeRole(projectId, currentUserId, new ChangeProjectMemberRoleCommand("MEMBER")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You cannot change your own role");

        verify(projectMembershipRepository, never()).save(org.mockito.Mockito.any());
    }
}