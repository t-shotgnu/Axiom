package com.studproj.axiom.application.features.taskrelationships;

import com.studproj.axiom.application.features.taskrelationships.createtaskrelationship.CreateTaskRelationshipCommand;
import com.studproj.axiom.application.features.taskrelationships.createtaskrelationship.CreateTaskRelationshipCommandHandler;
import com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship.DeleteTaskRelationshipCommand;
import com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship.DeleteTaskRelationshipCommandHandler;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByProjectQuery;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByProjectQueryHandler;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByWorkItemQuery;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByWorkItemQueryHandler;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.*;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.TaskRelationshipRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRelationshipHandlersTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SOURCE_ID = UUID.randomUUID();
    private static final UUID TARGET_ID = UUID.randomUUID();

    @Nested
    class CreateTaskRelationshipTests {
        @Mock private TaskRelationshipRepository taskRelationshipRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private CreateTaskRelationshipCommandHandler handler;

        private Project project;
        private WorkItem sourceItem;
        private WorkItem targetItem;
        private ProjectMembership membership;

        @BeforeEach
        void setUp() {
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
            membership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now());
        }

        private void setupValidItems(WorkItemType sourceType, WorkItemType targetType) {
            sourceItem = WorkItem.builder()
                    .id(SOURCE_ID).projectId(PROJECT_ID).type(sourceType).build();
            targetItem = WorkItem.builder()
                    .id(TARGET_ID).projectId(PROJECT_ID).type(targetType).build();

            when(workItemRepository.findById(SOURCE_ID)).thenReturn(Optional.of(sourceItem));
            when(workItemRepository.findById(TARGET_ID)).thenReturn(Optional.of(targetItem));
        }

        @Test
        void shouldCreateParentChildRelationshipSuccessfully() {
            setupValidItems(WorkItemType.UserStory, WorkItemType.Epic);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));
            when(taskRelationshipRepository.findBySourceIdAndTargetIdAndLinkType(SOURCE_ID, TARGET_ID, LinkType.ChildOf))
                    .thenReturn(Optional.empty());

            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(SOURCE_ID, TARGET_ID, LinkType.ChildOf);
            UUID resultId = handler.handle(command);

            assertThat(resultId).isNotNull();
            verify(taskRelationshipRepository).save(any(TaskRelationship.class));
        }

        @Test
        void shouldNormalizeParentOfToChildOfAndSwapSourceTarget() {
            setupValidItems(WorkItemType.UserStory, WorkItemType.Epic);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));
            when(taskRelationshipRepository.findBySourceIdAndTargetIdAndLinkType(SOURCE_ID, TARGET_ID, LinkType.ChildOf))
                    .thenReturn(Optional.empty());

            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(TARGET_ID, SOURCE_ID, LinkType.ParentOf);
            UUID resultId = handler.handle(command);

            assertThat(resultId).isNotNull();
            verify(taskRelationshipRepository).save(argThat(r -> 
                r.getSourceId().equals(SOURCE_ID) && 
                r.getTargetId().equals(TARGET_ID) && 
                r.getLinkType() == LinkType.ChildOf
            ));
        }

        @Test
        void shouldThrowDomainRuleViolationForInvalidLevels() {
            setupValidItems(WorkItemType.Epic, WorkItemType.UserStory);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));

            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(SOURCE_ID, TARGET_ID, LinkType.ChildOf);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessageContaining("Invalid parent-child relationship hierarchy");
        }

        @Test
        void shouldAllowSubtaskToSubtaskParenting() {
            setupValidItems(WorkItemType.Subtask, WorkItemType.Subtask);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));

            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(SOURCE_ID, TARGET_ID, LinkType.ChildOf);
            UUID result = handler.handle(command);
            assertThat(result).isNotNull();
        }

        @Test
        void shouldThrowWhenCyclicParentChildDetected() {
            setupValidItems(WorkItemType.UserStory, WorkItemType.Epic);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));

            UUID parentX = UUID.randomUUID();
            TaskRelationship link1 = TaskRelationship.builder().sourceId(TARGET_ID).targetId(parentX).linkType(LinkType.ChildOf).build();
            TaskRelationship link2 = TaskRelationship.builder().sourceId(parentX).targetId(SOURCE_ID).linkType(LinkType.ChildOf).build();

            when(taskRelationshipRepository.findBySourceIdAndLinkType(TARGET_ID, LinkType.ChildOf))
                    .thenReturn(List.of(link1));
            when(taskRelationshipRepository.findBySourceIdAndLinkType(parentX, LinkType.ChildOf))
                    .thenReturn(List.of(link2));

            when(taskRelationshipRepository.findBySourceIdAndLinkType(SOURCE_ID, LinkType.ChildOf))
                    .thenReturn(Collections.emptyList());

            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(SOURCE_ID, TARGET_ID, LinkType.ChildOf);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("Cyclic parent-child relationship detected.");
        }

        @Test
        void shouldThrowWhenCyclicDependencyDetected() {
            setupValidItems(WorkItemType.Task, WorkItemType.Task);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));

            TaskRelationship existingDep = TaskRelationship.builder().sourceId(TARGET_ID).targetId(SOURCE_ID).linkType(LinkType.BlockedBy).build();
            when(taskRelationshipRepository.findBySourceIdAndLinkType(TARGET_ID, LinkType.BlockedBy))
                    .thenReturn(List.of(existingDep));

            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(SOURCE_ID, TARGET_ID, LinkType.BlockedBy);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("Cyclic dependency relationship detected.");
        }

        @Test
        void shouldThrowWhenTasksFromDifferentProjects() {
            sourceItem = WorkItem.builder().id(SOURCE_ID).projectId(PROJECT_ID).type(WorkItemType.Task).build();
            targetItem = WorkItem.builder().id(TARGET_ID).projectId(UUID.randomUUID()).type(WorkItemType.Task).build();

            when(workItemRepository.findById(SOURCE_ID)).thenReturn(Optional.of(sourceItem));
            when(workItemRepository.findById(TARGET_ID)).thenReturn(Optional.of(targetItem));

            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(SOURCE_ID, TARGET_ID, LinkType.RelatesTo);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("Tasks must belong to the same project");
        }

        @Test
        void shouldThrowWhenSelfLinking() {
            CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(SOURCE_ID, SOURCE_ID, LinkType.RelatesTo);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("A work item cannot be linked to itself");
        }
    }

    @Nested
    class DeleteTaskRelationshipTests {
        @Mock private TaskRelationshipRepository taskRelationshipRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private DeleteTaskRelationshipCommandHandler handler;

        private TaskRelationship relationship;
        private WorkItem sourceItem;
        private Project project;
        private ProjectMembership membership;

        @BeforeEach
        void setUp() {
            relationship = TaskRelationship.builder().id(SOURCE_ID).sourceId(SOURCE_ID).targetId(TARGET_ID).linkType(LinkType.RelatesTo).build();
            sourceItem = WorkItem.builder().id(SOURCE_ID).projectId(PROJECT_ID).build();
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
            membership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now());
        }

        @Test
        void shouldDeleteSuccessfully() {
            when(taskRelationshipRepository.findById(SOURCE_ID)).thenReturn(Optional.of(relationship));
            when(workItemRepository.findById(SOURCE_ID)).thenReturn(Optional.of(sourceItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));

            handler.handle(new DeleteTaskRelationshipCommand(SOURCE_ID));

            verify(taskRelationshipRepository).delete(SOURCE_ID);
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(taskRelationshipRepository.findById(SOURCE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DeleteTaskRelationshipCommand(SOURCE_ID)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class GetTaskRelationshipsTests {
        @Mock private TaskRelationshipRepository taskRelationshipRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        
        @InjectMocks private GetTaskRelationshipsByProjectQueryHandler getByProjectHandler;
        @InjectMocks private GetTaskRelationshipsByWorkItemQueryHandler getByWorkItemHandler;

        private Project project;
        private ProjectMembership membership;

        @BeforeEach
        void setUp() {
            project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
            membership = new ProjectMembership(UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now());
        }

        @Test
        void shouldGetByProject() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));
            when(taskRelationshipRepository.findByProjectId(PROJECT_ID)).thenReturn(Collections.emptyList());

            var result = getByProjectHandler.handle(new GetTaskRelationshipsByProjectQuery(PROJECT_ID));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldGetByWorkItem() {
            WorkItem item = WorkItem.builder().id(SOURCE_ID).projectId(PROJECT_ID).build();
            when(workItemRepository.findById(SOURCE_ID)).thenReturn(Optional.of(item));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(Optional.of(membership));
            
            TaskRelationship link = TaskRelationship.builder().id(UUID.randomUUID()).sourceId(SOURCE_ID).targetId(TARGET_ID).linkType(LinkType.ChildOf).build();
            when(taskRelationshipRepository.findBySourceId(SOURCE_ID)).thenReturn(List.of(link));
            when(taskRelationshipRepository.findByTargetId(SOURCE_ID)).thenReturn(new ArrayList<>());

            var result = getByWorkItemHandler.handle(new GetTaskRelationshipsByWorkItemQuery(SOURCE_ID));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).sourceId()).isEqualTo(SOURCE_ID);
        }
    }
}
