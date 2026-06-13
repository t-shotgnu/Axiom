package com.studproj.axiom.application.features.workitems;

import com.studproj.axiom.application.features.workitems.assignworkitem.AssignWorkItemCommand;
import com.studproj.axiom.application.features.workitems.assignworkitem.AssignWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.createworkitem.CreateWorkItemCommand;
import com.studproj.axiom.application.features.workitems.createworkitem.CreateWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.deleteworkitem.DeleteWorkItemCommand;
import com.studproj.axiom.application.features.workitems.deleteworkitem.DeleteWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.getworkitembyid.GetWorkItemByIdQuery;
import com.studproj.axiom.application.features.workitems.getworkitembyid.GetWorkItemByIdQueryHandler;
import com.studproj.axiom.application.features.workitems.getworkitemsbyproject.GetWorkItemsByProjectQuery;
import com.studproj.axiom.application.features.workitems.getworkitemsbyproject.GetWorkItemsByProjectQueryHandler;
import com.studproj.axiom.application.features.workitems.updateworkitem.UpdateWorkItemCommand;
import com.studproj.axiom.application.features.workitems.updateworkitem.UpdateWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.updateworkitemnotes.UpdateWorkItemNotesCommand;
import com.studproj.axiom.application.features.workitems.updateworkitemnotes.UpdateWorkItemNotesCommandHandler;
import com.studproj.axiom.application.features.workitems.updateworkitemstatus.UpdateWorkItemStatusCommand;
import com.studproj.axiom.application.features.workitems.updateworkitemstatus.UpdateWorkItemStatusCommandHandler;
import com.studproj.axiom.domain.exception.AccessDeniedException;
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
class WorkItemHandlersTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WORK_ITEM_ID = UUID.randomUUID();

    @Nested
    class CreateWorkItemTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private UserRepository userRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private CreateWorkItemCommandHandler handler;

        private User user;
        private Project project;

        @BeforeEach
        void setUp() {
            user = User.builder().id(USER_ID).userName("u").emailAddress("u@e.com")
                    .password("p").createdOn(LocalDateTime.now()).active(true).build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
        }

        @Test
        void shouldCreateWorkItemSuccessfully() {
            when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("u@e.com");
            when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(workItemRepository.findMaxControlNoByProjectId(PROJECT_ID)).thenReturn(Optional.of(5));

            CreateWorkItemCommand command = new CreateWorkItemCommand(
                    "New task", 1, WorkItemType.Task, WorkItemStatus.New, null, null, PROJECT_ID, null);

            UUID result = handler.handle(command);

            assertThat(result).isNotNull();
            verify(workItemRepository).save(any(WorkItem.class));
        }

        @Test
        void shouldThrowWhenUserNotProjectMember() {
            when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("u@e.com");
            when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            CreateWorkItemCommand command = new CreateWorkItemCommand(
                    "Task", 1, WorkItemType.Task, WorkItemStatus.New, null, null, PROJECT_ID, null);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not a member of this project");
        }

        @Test
        void shouldThrowWhenAssigneeNotProjectMember() {
            UUID assigneeId = UUID.randomUUID();
            when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("u@e.com");
            when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, assigneeId)).thenReturn(false);

            CreateWorkItemCommand command = new CreateWorkItemCommand(
                    "Task", 1, WorkItemType.Task, WorkItemStatus.New, null, null, PROJECT_ID, assigneeId);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Assignee must be a member of this project");
        }

        @Test
        void shouldAssignFirstControlNoWhenNoneExist() {
            when(authenticatedUserProvider.getAuthenticatedUserEmail()).thenReturn("u@e.com");
            when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(workItemRepository.findMaxControlNoByProjectId(PROJECT_ID)).thenReturn(Optional.empty());

            CreateWorkItemCommand command = new CreateWorkItemCommand(
                    "First", 1, WorkItemType.Bug, WorkItemStatus.New, null, null, PROJECT_ID, null);

            UUID result = handler.handle(command);

            assertThat(result).isNotNull();
            verify(workItemRepository).save(any(WorkItem.class));
        }
    }

    @Nested
    class DeleteWorkItemTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private DeleteWorkItemCommandHandler handler;

        private WorkItem workItem;
        private Project project;

        @BeforeEach
        void setUp() {
            workItem = WorkItem.builder()
                    .id(WORK_ITEM_ID)
                    .controlNo(1)
                    .description("Test")
                    .type(WorkItemType.Task)
                    .status(WorkItemStatus.New)
                    .projectId(PROJECT_ID)
                    .authorId(USER_ID)
                    .build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
        }

        @Test
        void shouldDeleteWhenUserIsAuthor() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);

            handler.handle(new DeleteWorkItemCommand(WORK_ITEM_ID));

            verify(workItemRepository).delete(WORK_ITEM_ID);
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DeleteWorkItemCommand(WORK_ITEM_ID)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("WorkItem not found");
        }

        @Test
        void shouldThrowWhenNotProjectMember() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new DeleteWorkItemCommand(WORK_ITEM_ID)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not a member of this project");
        }

        @Test
        void shouldThrowWhenNotAuthorNorAdmin() {
            UUID otherUserId = UUID.randomUUID();
            WorkItem otherWorkItem = WorkItem.builder()
                    .id(WORK_ITEM_ID).controlNo(1).description("T")
                    .type(WorkItemType.Task).status(WorkItemStatus.New)
                    .projectId(PROJECT_ID).authorId(otherUserId).build();

            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(otherWorkItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            // Not admin
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DeleteWorkItemCommand(WORK_ITEM_ID)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class AssignWorkItemTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private AssignWorkItemCommandHandler handler;

        private WorkItem workItem;
        private Project project;

        @BeforeEach
        void setUp() {
            workItem = WorkItem.builder()
                    .id(WORK_ITEM_ID).controlNo(1).description("T")
                    .type(WorkItemType.Task).status(WorkItemStatus.New)
                    .projectId(PROJECT_ID).authorId(USER_ID).build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
        }

        @Test
        void shouldAssignSuccessfully() {
            UUID assigneeId = UUID.randomUUID();
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, assigneeId)).thenReturn(true);

            handler.handle(new AssignWorkItemCommand(WORK_ITEM_ID, assigneeId));

            assertThat(workItem.getAssigneeId()).isEqualTo(assigneeId);
            verify(workItemRepository).save(workItem);
        }

        @Test
        void shouldUnassignWhenNullAssignee() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);

            handler.handle(new AssignWorkItemCommand(WORK_ITEM_ID, null));

            assertThat(workItem.getAssigneeId()).isNull();
            verify(workItemRepository).save(workItem);
        }

        @Test
        void shouldThrowWhenAssigneeNotMember() {
            UUID assigneeId = UUID.randomUUID();
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, assigneeId)).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new AssignWorkItemCommand(WORK_ITEM_ID, assigneeId)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Assignee must be a member of this project");
        }
    }

    @Nested
    class UpdateWorkItemTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private UpdateWorkItemCommandHandler handler;

        private WorkItem workItem;
        private Project project;

        @BeforeEach
        void setUp() {
            workItem = WorkItem.builder()
                    .id(WORK_ITEM_ID).controlNo(1).description("T")
                    .type(WorkItemType.Task).status(WorkItemStatus.New)
                    .projectId(PROJECT_ID).authorId(USER_ID).build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
        }

        @Test
        void shouldUpdateWhenAuthor() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);

            handler.handle(new UpdateWorkItemCommand(WORK_ITEM_ID, "Updated", 2, WorkItemType.Bug, WorkItemStatus.Active, null, null, null, null));

            assertThat(workItem.getDescription()).isEqualTo("Updated");
            assertThat(workItem.getType()).isEqualTo(WorkItemType.Bug);
            verify(workItemRepository).save(workItem);
        }

        @Test
        void shouldThrowWhenNotAuthorNorAdmin() {
            UUID otherUser = UUID.randomUUID();
            WorkItem other = WorkItem.builder()
                    .id(WORK_ITEM_ID).controlNo(1).description("T")
                    .type(WorkItemType.Task).status(WorkItemStatus.New)
                    .projectId(PROJECT_ID).authorId(otherUser).build();

            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(other));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new UpdateWorkItemCommand(WORK_ITEM_ID, "X", 1, WorkItemType.Task, null, null, null, null, null)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class UpdateWorkItemNotesTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private UpdateWorkItemNotesCommandHandler handler;

        private WorkItem workItem;
        private Project project;

        @BeforeEach
        void setUp() {
            workItem = WorkItem.builder()
                    .id(WORK_ITEM_ID).controlNo(1).description("T")
                    .type(WorkItemType.Task).status(WorkItemStatus.New)
                    .projectId(PROJECT_ID).authorId(USER_ID).build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
        }

        @Test
        void shouldUpdateNotesWhenAuthor() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);

            handler.handle(new UpdateWorkItemNotesCommand(WORK_ITEM_ID, "Some notes"));

            assertThat(workItem.getNotes()).isEqualTo("Some notes");
            verify(workItemRepository).save(workItem);
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new UpdateWorkItemNotesCommand(WORK_ITEM_ID, "notes")))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class UpdateWorkItemStatusTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private UpdateWorkItemStatusCommandHandler handler;

        private WorkItem workItem;
        private Project project;

        @BeforeEach
        void setUp() {
            workItem = WorkItem.builder()
                    .id(WORK_ITEM_ID).controlNo(1).description("T")
                    .type(WorkItemType.Task).status(WorkItemStatus.New)
                    .projectId(PROJECT_ID).authorId(USER_ID).build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
        }

        @Test
        void shouldUpdateStatusSuccessfully() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);

            handler.handle(new UpdateWorkItemStatusCommand(WORK_ITEM_ID, WorkItemStatus.Active));

            assertThat(workItem.getStatus()).isEqualTo(WorkItemStatus.Active);
            verify(workItemRepository).save(workItem);
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new UpdateWorkItemStatusCommand(WORK_ITEM_ID, WorkItemStatus.Active)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("WorkItem not found");
        }

        @Test
        void shouldThrowWhenNotProjectMember() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new UpdateWorkItemStatusCommand(WORK_ITEM_ID, WorkItemStatus.Active)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not a member of this project");
        }
    }

    @Nested
    class GetWorkItemByIdTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private GetWorkItemByIdQueryHandler handler;

        private WorkItem workItem;
        private Project project;

        @BeforeEach
        void setUp() {
            workItem = WorkItem.builder()
                    .id(WORK_ITEM_ID).controlNo(1).description("T")
                    .type(WorkItemType.Task).status(WorkItemStatus.New)
                    .projectId(PROJECT_ID).authorId(USER_ID).build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
        }

        @Test
        void shouldReturnWorkItemWhenMember() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);

            var result = handler.handle(new GetWorkItemByIdQuery(WORK_ITEM_ID));

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(WORK_ITEM_ID);
        }

        @Test
        void shouldReturnEmptyWhenNotMember() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            var result = handler.handle(new GetWorkItemByIdQuery(WORK_ITEM_ID));

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            var result = handler.handle(new GetWorkItemByIdQuery(WORK_ITEM_ID));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetWorkItemsByProjectTests {
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private GetWorkItemsByProjectQueryHandler handler;

        @Test
        void shouldReturnItemsWhenMember() {
            Project project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
            WorkItem wi1 = WorkItem.builder().id(UUID.randomUUID()).controlNo(1).description("A")
                    .type(WorkItemType.Task).status(WorkItemStatus.New).projectId(PROJECT_ID).authorId(USER_ID).build();
            WorkItem wi2 = WorkItem.builder().id(UUID.randomUUID()).controlNo(2).description("B")
                    .type(WorkItemType.Bug).status(WorkItemStatus.Active).projectId(PROJECT_ID).authorId(USER_ID).build();

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(workItemRepository.findByProjectId(PROJECT_ID)).thenReturn(List.of(wi1, wi2));

            var result = handler.handle(new GetWorkItemsByProjectQuery(PROJECT_ID));

            assertThat(result).hasSize(2);
        }

        @Test
        void shouldThrowWhenNotMember() {
            Project project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new GetWorkItemsByProjectQuery(PROJECT_ID)))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void shouldReturnEmptyListWhenNoItems() {
            Project project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(workItemRepository.findByProjectId(PROJECT_ID)).thenReturn(List.of());

            var result = handler.handle(new GetWorkItemsByProjectQuery(PROJECT_ID));

            assertThat(result).isEmpty();
        }
    }
}
