package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateWorkItemCommand;
import com.studproj.axiom.application.dto.command.UpdateWorkItemCommand;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemCommandHandlerTest {

    @Mock
    private WorkItemRepository workItemRepository;

    @Mock
    private UserRepository userRepository;

        @Mock
        private ProjectRepository projectRepository;

        @Mock
        private ProjectMembershipRepository projectMembershipRepository;

        @Mock
        private ProjectRoleRepository projectRoleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;


    private WorkItemCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new WorkItemCommandHandler(
                workItemRepository,
                userRepository,
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("author@example.com", "password"));
        lenient().when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("author@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createWorkItemPersistsItemWithNextControlNumber() {
        UUID authorId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(authorId);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
        when(userRepository.findByEmail("author@example.com"))
                .thenReturn(Optional.of(User.builder().id(authorId).emailAddress("author@example.com").build()));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, authorId)).thenReturn(true);
        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, assigneeId)).thenReturn(true);
        when(workItemRepository.findMaxControlNoByProjectId(projectId)).thenReturn(Optional.of(41));

        UUID workItemId = handler.createWorkItem(new CreateWorkItemCommand(
                "Build tests",
                2,
                WorkItemType.Task,
                WorkItemStatus.New,
                dueDate,
                5,
                projectId,
                assigneeId
        ));

        ArgumentCaptor<WorkItem> captor = ArgumentCaptor.forClass(WorkItem.class);
        verify(workItemRepository).save(captor.capture());
        WorkItem savedWorkItem = captor.getValue();

        assertThat(workItemId).isEqualTo(savedWorkItem.getId());
        assertThat(savedWorkItem.getControlNo()).isEqualTo(42);
        assertThat(savedWorkItem.getDescription()).isEqualTo("Build tests");
        assertThat(savedWorkItem.getPriority()).isEqualTo(2);
        assertThat(savedWorkItem.getType()).isEqualTo(WorkItemType.Task);
        assertThat(savedWorkItem.getStatus()).isEqualTo(WorkItemStatus.New);
        assertThat(savedWorkItem.getDueDate()).isEqualTo(dueDate);
        assertThat(savedWorkItem.getEstimatedEffort()).isEqualTo(5);
        assertThat(savedWorkItem.getProjectId()).isEqualTo(projectId);
        assertThat(savedWorkItem.getAuthorId()).isEqualTo(authorId);
        assertThat(savedWorkItem.getAssigneeId()).isEqualTo(assigneeId);
    }

    @Test
    void createWorkItemStartsControlNumbersAtOne() {
        UUID authorId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(authorId);
        when(userRepository.findByEmail("author@example.com"))
                .thenReturn(Optional.of(User.builder().id(authorId).emailAddress("author@example.com").build()));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, authorId)).thenReturn(true);
        when(workItemRepository.findMaxControlNoByProjectId(projectId)).thenReturn(Optional.empty());

        handler.createWorkItem(new CreateWorkItemCommand(
                "First task",
                1,
                WorkItemType.Task,
                WorkItemStatus.New,
                null,
                null,
                projectId,
                null
        ));

        ArgumentCaptor<WorkItem> captor = ArgumentCaptor.forClass(WorkItem.class);
        verify(workItemRepository).save(captor.capture());
        assertThat(captor.getValue().getControlNo()).isEqualTo(1);
    }

    @Test
    void createWorkItemFailsWhenAuthenticatedUserCannotBeFound() {
        UUID projectId = UUID.randomUUID();
        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.createWorkItem(new CreateWorkItemCommand(
                "Task",
                1,
                WorkItemType.Task,
                WorkItemStatus.New,
                null,
                null,
                projectId,
                null
        )))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Authenticated user not found");
    }

        @Test
        void updateWorkItemAllowsAuthorToEditDetails() {
                UUID projectId = UUID.randomUUID();
                UUID authorId = UUID.randomUUID();
                UUID workItemId = UUID.randomUUID();
                when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(authorId);
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
                when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, authorId)).thenReturn(true);
                when(workItemRepository.findById(workItemId)).thenReturn(Optional.of(WorkItem.builder().id(workItemId).projectId(projectId).authorId(authorId).build()));

                handler.updateWorkItem(workItemId, new UpdateWorkItemCommand("Updated", 3, WorkItemType.Bug, null, 8, "note", null));

                verify(workItemRepository).save(org.mockito.ArgumentMatchers.any(WorkItem.class));
        }

        @Test
        void updateWorkItemAssignsMemberWhenAssigneeProvided() {
                UUID projectId = UUID.randomUUID();
                UUID authorId = UUID.randomUUID();
                UUID workItemId = UUID.randomUUID();
                UUID assigneeId = UUID.randomUUID();
                WorkItem existing = WorkItem.builder()
                                .id(workItemId)
                                .projectId(projectId)
                                .authorId(authorId)
                                .assigneeId(null)
                                .build();

                when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(authorId);
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
                when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, authorId)).thenReturn(true);
                when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, assigneeId)).thenReturn(true);
                when(workItemRepository.findById(workItemId)).thenReturn(Optional.of(existing));

                handler.updateWorkItem(workItemId, new UpdateWorkItemCommand("Updated", 3, WorkItemType.Bug, null, 8, "note", assigneeId));

                ArgumentCaptor<WorkItem> captor = ArgumentCaptor.forClass(WorkItem.class);
                verify(workItemRepository).save(captor.capture());
                assertThat(captor.getValue().getAssigneeId()).isEqualTo(assigneeId);
        }

        @Test
        void updateWorkItemRejectsAssigneeOutsideProject() {
                UUID projectId = UUID.randomUUID();
                UUID authorId = UUID.randomUUID();
                UUID workItemId = UUID.randomUUID();
                UUID assigneeId = UUID.randomUUID();

                when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(authorId);
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
                when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, authorId)).thenReturn(true);
                when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, assigneeId)).thenReturn(false);
                when(workItemRepository.findById(workItemId)).thenReturn(Optional.of(WorkItem.builder().id(workItemId).projectId(projectId).authorId(authorId).build()));

                assertThatThrownBy(() -> handler.updateWorkItem(workItemId, new UpdateWorkItemCommand("Updated", 3, WorkItemType.Bug, null, 8, "note", assigneeId)))
                                .isInstanceOf(com.studproj.axiom.domain.exception.ForbiddenException.class)
                                .hasMessage("Assignee must be a member of this project");

                verify(workItemRepository, never()).save(org.mockito.ArgumentMatchers.any(WorkItem.class));
        }

        @Test
        void deleteWorkItemRejectsNonAuthorNonAdmin() {
                UUID projectId = UUID.randomUUID();
                UUID authorId = UUID.randomUUID();
                UUID currentUserId = UUID.randomUUID();
                UUID workItemId = UUID.randomUUID();
                when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(currentUserId);
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
                when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, currentUserId)).thenReturn(true);
                when(projectMembershipRepository.findByProjectIdAndUserId(projectId, currentUserId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.ProjectMembership.builder().id(UUID.randomUUID()).projectId(projectId).userId(currentUserId).roleId(UUID.randomUUID()).build()));
                when(projectRoleRepository.findById(org.mockito.ArgumentMatchers.any())).thenReturn(Optional.of(com.studproj.axiom.domain.model.ProjectRole.builder().id(UUID.randomUUID()).type(com.studproj.axiom.domain.model.ProjectRoleType.MEMBER).build()));
                when(workItemRepository.findById(workItemId)).thenReturn(Optional.of(WorkItem.builder().id(workItemId).projectId(projectId).authorId(authorId).build()));

                assertThatThrownBy(() -> handler.deleteWorkItem(workItemId))
                                .isInstanceOf(com.studproj.axiom.domain.exception.ForbiddenException.class);
        }
}
