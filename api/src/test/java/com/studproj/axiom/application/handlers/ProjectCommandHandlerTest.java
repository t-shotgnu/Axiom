package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateProjectCommand;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCommandHandlerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMembershipRepository projectMembershipRepository;

    @Mock
    private ProjectRoleRepository projectRoleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;


    private ProjectCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProjectCommandHandler(
            projectRepository,
            userRepository,
            projectMembershipRepository,
            projectRoleRepository,
            authenticatedUserProvider);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("owner@example.com", "password"));
        org.mockito.Mockito.lenient().when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("owner@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProjectPersistsProjectForAuthenticatedUser() {
        UUID ownerId = UUID.randomUUID();
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(User.builder().id(ownerId).emailAddress("owner@example.com").build()));
        when(projectRoleRepository.findByType(ProjectRoleType.ADMIN))
            .thenReturn(Optional.of(ProjectRole.builder().id(UUID.randomUUID()).type(ProjectRoleType.ADMIN).build()));

        UUID projectId = handler.createProject(new CreateProjectCommand("Axiom", "AX", "Main project"));

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        Project savedProject = captor.getValue();

        assertThat(projectId).isEqualTo(savedProject.getId());
        assertThat(savedProject.getName()).isEqualTo("Axiom");
        assertThat(savedProject.getCode()).isEqualTo("AX");
        assertThat(savedProject.getDescription()).isEqualTo("Main project");
        assertThat(savedProject.getOwnerId()).isEqualTo(ownerId);
        assertThat(savedProject.getCreatedOn()).isNotNull();
        verify(projectMembershipRepository).save(org.mockito.ArgumentMatchers.any(ProjectMembership.class));
    }

    @Test
    void createProjectFailsWhenAuthenticatedUserCannotBeFound() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.createProject(new CreateProjectCommand("Axiom", "AX", null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteProjectDelegatesToRepository() {
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder().id(projectId).build()));

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(ownerId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, ownerId))
            .thenReturn(Optional.of(ProjectMembership.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .userId(ownerId)
                .roleId(roleId)
                .build()));
        when(projectRoleRepository.findById(roleId))
            .thenReturn(Optional.of(ProjectRole.builder().id(roleId).type(ProjectRoleType.ADMIN).build()));

        handler.deleteProject(projectId);

        verify(projectMembershipRepository).deleteByProjectId(projectId);
        verify(projectRepository).delete(projectId);
    }
}
