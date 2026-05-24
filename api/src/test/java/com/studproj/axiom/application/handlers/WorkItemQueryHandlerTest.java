package com.studproj.axiom.application.handlers;

import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkItemQueryHandlerTest {

    private final WorkItemRepository workItemRepository = mock(WorkItemRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMembershipRepository projectMembershipRepository = mock(ProjectMembershipRepository.class);
    private final AuthenticatedUserProvider authenticatedUserProvider = mock(AuthenticatedUserProvider.class);
    private final WorkItemQueryHandler handler = new WorkItemQueryHandler(
            workItemRepository,
            projectRepository,
            projectMembershipRepository,
            authenticatedUserProvider);

    @Test
    void getWorkItemsByProjectMapsDomainModelsToDtos() {
        UUID projectId = UUID.randomUUID();
        UUID workItemId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(true);
        when(workItemRepository.findByProjectId(projectId)).thenReturn(List.of(WorkItem.builder()
                .id(workItemId)
                .controlNo(7)
                .description("Build tests")
                .priority(2)
                .type(WorkItemType.Task)
                .status(WorkItemStatus.Active)
                .dueDate(dueDate)
                .estimatedEffort(4)
                .projectId(projectId)
                .authorId(authorId)
                .assigneeId(assigneeId)
                .build()));

        var items = handler.getWorkItemsByProject(projectId);

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().id()).isEqualTo(workItemId);
        assertThat(items.getFirst().controlNo()).isEqualTo(7);
        assertThat(items.getFirst().description()).isEqualTo("Build tests");
        assertThat(items.getFirst().priority()).isEqualTo(2);
        assertThat(items.getFirst().type()).isEqualTo(WorkItemType.Task);
        assertThat(items.getFirst().status()).isEqualTo(WorkItemStatus.Active);
        assertThat(items.getFirst().dueDate()).isEqualTo(dueDate);
        assertThat(items.getFirst().estimatedEffort()).isEqualTo(4);
        assertThat(items.getFirst().projectId()).isEqualTo(projectId);
        assertThat(items.getFirst().authorId()).isEqualTo(authorId);
        assertThat(items.getFirst().assigneeId()).isEqualTo(assigneeId);
    }

    @Test
    void getWorkItemByIdReturnsMappedDtoWhenFound() {
        UUID workItemId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(workItemRepository.findById(workItemId)).thenReturn(Optional.of(WorkItem.builder()
                .id(workItemId)
                .controlNo(1)
                .description("Task")
                .priority(1)
                .type(WorkItemType.Task)
                .status(WorkItemStatus.New)
            .projectId(projectId)
                .authorId(UUID.randomUUID())
                .build()));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(com.studproj.axiom.domain.model.Project.builder().id(projectId).build()));
        when(projectMembershipRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(true);

        var item = handler.getWorkItemById(workItemId);

        assertThat(item).isPresent();
        assertThat(item.orElseThrow().id()).isEqualTo(workItemId);
        assertThat(item.orElseThrow().status()).isEqualTo(WorkItemStatus.New);
    }

    @Test
    void getWorkItemByIdReturnsEmptyWhenMissing() {
        UUID workItemId = UUID.randomUUID();
        when(workItemRepository.findById(workItemId)).thenReturn(Optional.empty());

        assertThat(handler.getWorkItemById(workItemId)).isEmpty();
    }
}
