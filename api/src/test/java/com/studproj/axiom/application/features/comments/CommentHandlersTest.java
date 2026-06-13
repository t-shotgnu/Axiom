package com.studproj.axiom.application.features.comments;

import com.studproj.axiom.application.features.comments.createcomment.CreateCommentCommand;
import com.studproj.axiom.application.features.comments.createcomment.CreateCommentCommandHandler;
import com.studproj.axiom.application.features.comments.deletecomment.DeleteCommentCommand;
import com.studproj.axiom.application.features.comments.deletecomment.DeleteCommentCommandHandler;
import com.studproj.axiom.application.features.comments.getcommentsbyworkitem.GetCommentsByWorkItemQuery;
import com.studproj.axiom.application.features.comments.getcommentsbyworkitem.GetCommentsByWorkItemQueryHandler;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.domain.repository.*;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.infrastructure.persistence.entity.CommentEntity;
import com.studproj.axiom.infrastructure.persistence.repository.CommentJpaRepository;
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
class CommentHandlersTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WORK_ITEM_ID = UUID.randomUUID();

    private WorkItem workItem;
    private Project project;

    @BeforeEach
    void setUpCommon() {
        workItem = WorkItem.builder()
                .id(WORK_ITEM_ID)
                .controlNo(1)
                .description("Test")
                .type(WorkItemType.Task)
                .status(WorkItemStatus.New)
                .projectId(PROJECT_ID)
                .authorId(USER_ID)
                .build();
        project = new Project(PROJECT_ID, "Test Project", "TP", "desc", LocalDateTime.now(), USER_ID);
    }

    @Nested
    class CreateCommentTests {
        @Mock private CommentRepository commentRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private CreateCommentCommandHandler handler;

        @Test
        void shouldCreateCommentSuccessfully() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);

            UUID result = handler.handle(new CreateCommentCommand(WORK_ITEM_ID, "My comment"));

            assertThat(result).isNotNull();
            verify(commentRepository).save(any());
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new CreateCommentCommand(WORK_ITEM_ID, "text")))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("WorkItem not found");
        }

        @Test
        void shouldThrowWhenUserNotProjectMember() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new CreateCommentCommand(WORK_ITEM_ID, "text")))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not a member of this project");
        }
    }

    @Nested
    class DeleteCommentTests {
        @Mock private CommentJpaRepository commentRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private DeleteCommentCommandHandler handler;

        private UUID commentId;
        private CommentEntity commentEntity;

        @BeforeEach
        void setUp() {
            commentId = UUID.randomUUID();
            commentEntity = CommentEntity.builder()
                    .id(commentId)
                    .workItemId(WORK_ITEM_ID)
                    .authorId(USER_ID)
                    .text("comment text")
                    .createdOn(LocalDateTime.now())
                    .build();
        }

        @Test
        void shouldDeleteWhenUserIsAuthor() {
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentEntity));
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);

            handler.handle(new DeleteCommentCommand(commentId));

            verify(commentRepository).deleteById(commentId);
        }

        @Test
        void shouldThrowWhenCommentNotFound() {
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DeleteCommentCommand(commentId)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Comment not found");
        }

        @Test
        void shouldThrowWhenNotAuthorNorAdmin() {
            UUID otherUserId = UUID.randomUUID();
            commentEntity.setAuthorId(otherUserId);

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentEntity));
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            // Not admin — no membership with admin role found
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DeleteCommentCommand(commentId)))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void shouldThrowWhenNotProjectMember() {
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentEntity));
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new DeleteCommentCommand(commentId)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not a member of this project");
        }
    }

    @Nested
    class GetCommentsByWorkItemTests {
        @Mock private CommentJpaRepository commentRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @Mock private UserRepository userRepository;
        @InjectMocks private GetCommentsByWorkItemQueryHandler handler;

        @Test
        void shouldReturnCommentsWhenMember() {
            CommentEntity c1 = CommentEntity.builder().id(UUID.randomUUID()).workItemId(WORK_ITEM_ID)
                    .authorId(USER_ID).text("First").createdOn(LocalDateTime.now()).build();
            CommentEntity c2 = CommentEntity.builder().id(UUID.randomUUID()).workItemId(WORK_ITEM_ID)
                    .authorId(USER_ID).text("Second").createdOn(LocalDateTime.now()).build();
            com.studproj.axiom.domain.model.User user = com.studproj.axiom.domain.model.User.builder()
                    .id(USER_ID).userName("u").emailAddress("e").password("p")
                    .firstName("John").lastName("Doe").createdOn(LocalDateTime.now()).active(true).build();

            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(commentRepository.findByWorkItemIdOrderByCreatedOnAsc(WORK_ITEM_ID)).thenReturn(List.of(c1, c2));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            var result = handler.handle(new GetCommentsByWorkItemQuery(WORK_ITEM_ID));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).text()).isEqualTo("First");
            assertThat(result.get(0).author()).isEqualTo("John Doe");
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new GetCommentsByWorkItemQuery(WORK_ITEM_ID)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("WorkItem not found");
        }

        @Test
        void shouldThrowWhenNotMember() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new GetCommentsByWorkItemQuery(WORK_ITEM_ID)))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void shouldReturnEmptyListWhenNoComments() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(commentRepository.findByWorkItemIdOrderByCreatedOnAsc(WORK_ITEM_ID)).thenReturn(List.of());

            var result = handler.handle(new GetCommentsByWorkItemQuery(WORK_ITEM_ID));

            assertThat(result).isEmpty();
        }
    }
}
