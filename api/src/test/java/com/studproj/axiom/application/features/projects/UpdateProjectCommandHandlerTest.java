package com.studproj.axiom.application.features.projects;

import com.studproj.axiom.application.features.projects.updateproject.UpdateProjectCommand;
import com.studproj.axiom.application.features.projects.updateproject.UpdateProjectCommandHandler;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProjectCommandHandlerTest {

    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID adminRoleId = UUID.randomUUID();
    private final LocalDateTime createdOn = LocalDateTime.of(2026, 1, 1, 12, 0);
    private Project existingProject;
    private ProjectMembership adminMembership;

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMembershipRepository projectMembershipRepository;
    @Mock private ProjectRoleRepository projectRoleRepository;
    @Mock private AuthenticatedUserProvider authenticatedUserProvider;
    @InjectMocks private UpdateProjectCommandHandler handler;

    @BeforeEach
    void setUp() {
        existingProject = new Project(projectId, "Axiom", "AX", "Description", createdOn, userId);
        adminMembership = new ProjectMembership(UUID.randomUUID(), projectId, userId, adminRoleId, createdOn);
    }

    @Test
    void updatesProjectNameAndNormalizesCodeWhenAdmin() throws Exception {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(adminMembership));
        when(projectRoleRepository.findById(adminRoleId))
                .thenReturn(Optional.of(new ProjectRole(adminRoleId, ProjectRoleType.ADMIN)));
        when(projectRepository.findByCode("RP")).thenReturn(Optional.empty());

        handler.handle(projectId, new UpdateProjectCommand("  Renamed project  ", " rp "));

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        Project saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(projectId);
        assertThat(saved.getName()).isEqualTo("Renamed project");
        assertThat(saved.getCode()).isEqualTo("RP");
        assertThat(saved.getDescription()).isEqualTo("Description");
        assertThat(saved.getCreatedOn()).isEqualTo(createdOn);
        assertThat(saved.getOwnerId()).isEqualTo(userId);
    }

    @Test
    void allowsKeepingTheSameProjectCode() throws Exception {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(adminMembership));
        when(projectRoleRepository.findById(adminRoleId))
                .thenReturn(Optional.of(new ProjectRole(adminRoleId, ProjectRoleType.ADMIN)));
        when(projectRepository.findByCode("AX")).thenReturn(Optional.of(existingProject));

        handler.handle(projectId, new UpdateProjectCommand("Axiom renamed", "ax"));

        verify(projectRepository).save(org.mockito.ArgumentMatchers.any(Project.class));
    }

    @Test
    void rejectsDuplicateProjectCodesOwnedByAnotherProject() {
        Project otherProject = new Project(UUID.randomUUID(), "Other", "RP", "", createdOn, userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(adminMembership));
        when(projectRoleRepository.findById(adminRoleId))
                .thenReturn(Optional.of(new ProjectRole(adminRoleId, ProjectRoleType.ADMIN)));
        when(projectRepository.findByCode("RP")).thenReturn(Optional.of(otherProject));

        assertThatThrownBy(() -> handler.handle(projectId, new UpdateProjectCommand("Renamed", "rp")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Project code already exists");

        verify(projectRepository, never()).save(org.mockito.ArgumentMatchers.any(Project.class));
    }

    @Test
    void rejectsUpdatesWhenUserCannotManageProject() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectMembershipRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(projectId, new UpdateProjectCommand("Renamed", "RP")))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this project");

        verify(projectRepository, never()).save(org.mockito.ArgumentMatchers.any(Project.class));
    }
}
