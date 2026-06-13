package com.studproj.axiom.application.features.projectmembers;

import com.studproj.axiom.application.features.projectmembers.addprojectmember.AddProjectMemberCommand;
import com.studproj.axiom.application.features.projectmembers.addprojectmember.AddProjectMemberCommandHandler;
import com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole.ChangeProjectMemberRoleCommand;
import com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole.ChangeProjectMemberRoleCommandHandler;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.GetProjectMembersQuery;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.GetProjectMembersQueryHandler;
import com.studproj.axiom.application.features.projectmembers.removeprojectmember.RemoveProjectMemberCommand;
import com.studproj.axiom.application.features.projectmembers.removeprojectmember.RemoveProjectMemberCommandHandler;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
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
class ProjectMemberHandlersTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID ADMIN_USER_ID = UUID.randomUUID();
    private static final UUID ADMIN_ROLE_ID = UUID.randomUUID();

    private Project project;
    private ProjectRole adminRole;
    private ProjectRole memberRole;
    private ProjectMembership adminMembership;

    @BeforeEach
    void setUpCommon() {
        project = new Project(PROJECT_ID, "Test", "T", "desc", LocalDateTime.now(), ADMIN_USER_ID);
        adminRole = new ProjectRole(ADMIN_ROLE_ID, ProjectRoleType.ADMIN);
        memberRole = new ProjectRole(UUID.randomUUID(), ProjectRoleType.MEMBER);
        adminMembership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, ADMIN_USER_ID, ADMIN_ROLE_ID, LocalDateTime.now());
    }

    private void mockAdminAccess(ProjectRepository projectRepository,
                                  ProjectMembershipRepository membershipRepository,
                                  ProjectRoleRepository roleRepository,
                                  AuthenticatedUserProvider authProvider) {
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(authProvider.getAuthenticatedUserId()).thenReturn(ADMIN_USER_ID);
        when(membershipRepository.findByProjectIdAndUserId(PROJECT_ID, ADMIN_USER_ID)).thenReturn(Optional.of(adminMembership));
        when(roleRepository.findById(ADMIN_ROLE_ID)).thenReturn(Optional.of(adminRole));
    }

    @Nested
    class AddProjectMemberTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private UserRepository userRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private AddProjectMemberCommandHandler handler;

        @Test
        void shouldAddMemberSuccessfully() {
            UUID newUserId = UUID.randomUUID();
            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, newUserId)).thenReturn(false);
            when(userRepository.findById(newUserId)).thenReturn(Optional.of(
                    User.builder().id(newUserId).userName("new").emailAddress("n@e.com")
                            .password("p").createdOn(LocalDateTime.now()).active(true).build()));
            when(projectRoleRepository.findByType(ProjectRoleType.MEMBER)).thenReturn(Optional.of(memberRole));

            handler.handle(PROJECT_ID, new AddProjectMemberCommand(newUserId, ProjectRoleType.MEMBER));

            verify(projectMembershipRepository).save(any(ProjectMembership.class));
        }

        @Test
        void shouldThrowWhenUserAlreadyMember() {
            UUID existingUserId = UUID.randomUUID();
            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, existingUserId)).thenReturn(true);

            assertThatThrownBy(() -> handler.handle(PROJECT_ID, new AddProjectMemberCommand(existingUserId, ProjectRoleType.MEMBER)))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("User is already a member of this project");
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            UUID unknownId = UUID.randomUUID();
            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, unknownId)).thenReturn(false);
            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(PROJECT_ID, new AddProjectMemberCommand(unknownId, ProjectRoleType.MEMBER)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    class RemoveProjectMemberTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private RemoveProjectMemberCommandHandler handler;

        @Test
        void shouldRemoveMemberSuccessfully() {
            UUID targetUserId = UUID.randomUUID();
            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, targetUserId))
                    .thenReturn(Optional.of(new ProjectMembership(UUID.randomUUID(), PROJECT_ID, targetUserId, memberRole.getId(), LocalDateTime.now())));

            handler.handle(new RemoveProjectMemberCommand(PROJECT_ID, targetUserId));

            verify(projectMembershipRepository).deleteByProjectIdAndUserId(PROJECT_ID, targetUserId);
        }

        @Test
        void shouldThrowWhenRemovingProjectLead() {
            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> handler.handle(new RemoveProjectMemberCommand(PROJECT_ID, ADMIN_USER_ID)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Project lead cannot be removed from the project");
        }

        @Test
        void shouldThrowWhenRemovingSelf() {
            // Project owner is ADMIN_USER_ID, but let's make target a different user who is also the caller
            UUID callerId = UUID.randomUUID();
            Project otherProject = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), UUID.randomUUID());
            UUID callerRoleId = UUID.randomUUID();
            ProjectMembership callerMembership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, callerId, callerRoleId, LocalDateTime.now());

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(otherProject));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(callerId);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, callerId)).thenReturn(Optional.of(callerMembership));
            when(projectRoleRepository.findById(callerRoleId)).thenReturn(Optional.of(adminRole));

            assertThatThrownBy(() -> handler.handle(new RemoveProjectMemberCommand(PROJECT_ID, callerId)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You cannot remove yourself from the project");
        }

        @Test
        void shouldThrowWhenMemberNotFound() {
            UUID targetUserId = UUID.randomUUID();
            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, targetUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new RemoveProjectMemberCommand(PROJECT_ID, targetUserId)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Project member not found");
        }
    }

    @Nested
    class ChangeProjectMemberRoleTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private ChangeProjectMemberRoleCommandHandler handler;

        @Test
        void shouldChangeRoleSuccessfully() {
            UUID targetUserId = UUID.randomUUID();
            ProjectMembership targetMembership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, targetUserId, memberRole.getId(), LocalDateTime.now());

            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, targetUserId)).thenReturn(Optional.of(targetMembership));
            when(projectRoleRepository.findByType(ProjectRoleType.ADMIN)).thenReturn(Optional.of(adminRole));

            handler.handle(PROJECT_ID, targetUserId, new ChangeProjectMemberRoleCommand(ProjectRoleType.ADMIN));

            verify(projectMembershipRepository).save(targetMembership);
        }

        @Test
        void shouldThrowWhenChangingOwnRole() {
            mockAdminAccess(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider);

            assertThatThrownBy(() -> handler.handle(PROJECT_ID, ADMIN_USER_ID, new ChangeProjectMemberRoleCommand(ProjectRoleType.MEMBER)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You cannot change your own role");
        }

        @Test
        void shouldThrowWhenChangingProjectLeadRole() {
            UUID targetUserId = ADMIN_USER_ID; // owner
            UUID callerId = UUID.randomUUID();
            UUID callerRoleId = UUID.randomUUID();
            ProjectMembership callerMembership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, callerId, callerRoleId, LocalDateTime.now());

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(callerId);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, callerId)).thenReturn(Optional.of(callerMembership));
            when(projectRoleRepository.findById(callerRoleId)).thenReturn(Optional.of(adminRole));

            assertThatThrownBy(() -> handler.handle(PROJECT_ID, targetUserId, new ChangeProjectMemberRoleCommand(ProjectRoleType.MEMBER)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Project lead role cannot be changed");
        }
    }

    @Nested
    class GetProjectMembersTests {
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private UserRepository userRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private GetProjectMembersQueryHandler handler;

        @Test
        void shouldReturnMembersWhenMember() {
            UUID memberId = UUID.randomUUID();
            UUID memberRoleId = memberRole.getId();
            ProjectMembership membership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, memberId, memberRoleId, LocalDateTime.now());
            User memberUser = User.builder().id(memberId).userName("member").emailAddress("m@e.com")
                    .password("p").firstName("M").lastName("U").createdOn(LocalDateTime.now()).active(true).build();

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(ADMIN_USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, ADMIN_USER_ID))
                    .thenReturn(Optional.of(adminMembership));
            when(projectMembershipRepository.findByProjectId(PROJECT_ID)).thenReturn(List.of(membership));
            when(userRepository.findById(memberId)).thenReturn(Optional.of(memberUser));
            when(projectRoleRepository.findById(memberRoleId)).thenReturn(Optional.of(memberRole));

            var result = handler.handle(new GetProjectMembersQuery(PROJECT_ID));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).userName()).isEqualTo("member");
            assertThat(result.get(0).role()).isEqualTo(ProjectRoleType.MEMBER);
        }

        @Test
        void shouldThrowWhenNotMember() {
            UUID outsiderId = UUID.randomUUID();
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(outsiderId);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, outsiderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new GetProjectMembersQuery(PROJECT_ID)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }
}
