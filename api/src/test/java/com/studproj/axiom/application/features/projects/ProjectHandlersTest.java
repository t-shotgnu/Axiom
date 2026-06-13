package com.studproj.axiom.application.features.projects;

import com.studproj.axiom.application.features.projects.createproject.CreateProjectCommand;
import com.studproj.axiom.application.features.projects.createproject.CreateProjectCommandHandler;
import com.studproj.axiom.application.features.projects.deleteproject.DeleteProjectCommand;
import com.studproj.axiom.application.features.projects.deleteproject.DeleteProjectCommandHandler;
import com.studproj.axiom.application.features.projects.getallprojects.GetAllProjectsQuery;
import com.studproj.axiom.application.features.projects.getallprojects.GetAllProjectsQueryHandler;
import com.studproj.axiom.application.features.projects.getprojectdetails.GetProjectDetailsQuery;
import com.studproj.axiom.application.features.projects.getprojectdetails.GetProjectDetailsQueryHandler;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.*;
import com.studproj.axiom.domain.repository.*;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectHandlersTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID PROJECT_ID = UUID.randomUUID();

    @Nested
    class CreateProjectTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private UserRepository userRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private CreateProjectCommandHandler handler;

        private User user;

        @BeforeEach
        void setUp() {
            user = User.builder().id(USER_ID).userName("u").emailAddress("u@e.com")
                    .password("p").createdOn(LocalDateTime.now()).active(true).build();
        }

        @Test
        void shouldCreateProjectAndAssignAdmin() {
            UUID adminRoleId = UUID.randomUUID();
            when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("u@e.com");
            when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
            when(projectRoleRepository.findByType(ProjectRoleType.ADMIN))
                    .thenReturn(Optional.of(new ProjectRole(adminRoleId, ProjectRoleType.ADMIN)));

            UUID result = handler.handle(new CreateProjectCommand("My Project", "MP", "desc"));

            assertThat(result).isNotNull();
            verify(projectRepository).save(any(Project.class));
            verify(projectMembershipRepository).save(any(ProjectMembership.class));
        }

        @Test
        void shouldThrowWhenAdminRoleNotFound() {
            when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("u@e.com");
            when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
            when(projectRoleRepository.findByType(ProjectRoleType.ADMIN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new CreateProjectCommand("P", "P", null)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Project role ADMIN not found");
        }
    }

    @Nested
    class DeleteProjectTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private DeleteProjectCommandHandler handler;

        @Test
        void shouldDeleteProjectWhenAdmin() {
            UUID adminRoleId = UUID.randomUUID();
            Project project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
            ProjectMembership membership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, USER_ID, adminRoleId, LocalDateTime.now());

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));
            when(projectRoleRepository.findById(adminRoleId)).thenReturn(Optional.of(new ProjectRole(adminRoleId, ProjectRoleType.ADMIN)));

            handler.handle(new DeleteProjectCommand(PROJECT_ID));

            verify(projectMembershipRepository).deleteByProjectId(PROJECT_ID);
            verify(projectRepository).delete(PROJECT_ID);
        }

        @Test
        void shouldThrowWhenProjectNotFound() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DeleteProjectCommand(PROJECT_ID)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class GetAllProjectsTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private UserRepository userRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private GetAllProjectsQueryHandler handler;

        @Test
        void shouldReturnProjectsForUser() {
            Project p1 = new Project(UUID.randomUUID(), "P1", "P1", "d", LocalDateTime.now(), USER_ID);
            Project p2 = new Project(UUID.randomUUID(), "P2", "P2", "d", LocalDateTime.now(), USER_ID);
            ProjectMembership m1 = new ProjectMembership(UUID.randomUUID(), p1.getId(), USER_ID, UUID.randomUUID(), LocalDateTime.now());
            ProjectMembership m2 = new ProjectMembership(UUID.randomUUID(), p2.getId(), USER_ID, UUID.randomUUID(), LocalDateTime.now());
            User user = User.builder().id(USER_ID).userName("u").emailAddress("e@e.com")
                    .password("p").firstName("John").lastName("Doe").createdOn(LocalDateTime.now()).active(true).build();

            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByUserId(USER_ID)).thenReturn(List.of(m1, m2));
            when(projectRepository.findByIds(List.of(p1.getId(), p2.getId()))).thenReturn(List.of(p1, p2));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            var result = handler.handle(new GetAllProjectsQuery());

            assertThat(result).hasSize(2);
            assertThat(result.get(0).ownerName()).isEqualTo("John Doe");
        }

        @Test
        void shouldReturnEmptyWhenNoMemberships() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(projectRepository.findByIds(List.of())).thenReturn(List.of());

            var result = handler.handle(new GetAllProjectsQuery());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetProjectDetailsTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private UserRepository userRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private GetProjectDetailsQueryHandler handler;

        @Test
        void shouldReturnProjectWhenMember() {
            Project project = new Project(PROJECT_ID, "Proj", "PR", "desc", LocalDateTime.now(), USER_ID);
            User user = User.builder().id(USER_ID).userName("owner").emailAddress("o@e.com")
                    .password("p").firstName("Jane").lastName("Smith").createdOn(LocalDateTime.now()).active(true).build();

            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            var result = handler.handle(new GetProjectDetailsQuery(PROJECT_ID));

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Proj");
            assertThat(result.get().ownerName()).isEqualTo("Jane Smith");
        }

        @Test
        void shouldReturnEmptyWhenNotMember() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            var result = handler.handle(new GetProjectDetailsQuery(PROJECT_ID));

            assertThat(result).isEmpty();
        }
    }
}
