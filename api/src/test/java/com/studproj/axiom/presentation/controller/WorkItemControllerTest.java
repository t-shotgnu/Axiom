package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.workitems.WorkItemDto;
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
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.presentation.controller.dto.AssignWorkItemRequest;
import com.studproj.axiom.presentation.controller.dto.UpdateWorkItemNotesRequest;
import com.studproj.axiom.presentation.controller.dto.UpdateWorkItemRequest;
import com.studproj.axiom.presentation.controller.dto.UpdateWorkItemStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkItemControllerTest {

    private CreateWorkItemCommandHandler createHandler;
    private AssignWorkItemCommandHandler assignHandler;
    private UpdateWorkItemCommandHandler updateHandler;
    private UpdateWorkItemStatusCommandHandler updateStatusHandler;
    private UpdateWorkItemNotesCommandHandler updateNotesHandler;
    private DeleteWorkItemCommandHandler deleteHandler;
    private GetWorkItemByIdQueryHandler getByIdHandler;
    private GetWorkItemsByProjectQueryHandler getByProjectHandler;
    private WorkItemController controller;

    @BeforeEach
    void setUp() {
        createHandler = mock(CreateWorkItemCommandHandler.class);
        assignHandler = mock(AssignWorkItemCommandHandler.class);
        updateHandler = mock(UpdateWorkItemCommandHandler.class);
        updateStatusHandler = mock(UpdateWorkItemStatusCommandHandler.class);
        updateNotesHandler = mock(UpdateWorkItemNotesCommandHandler.class);
        deleteHandler = mock(DeleteWorkItemCommandHandler.class);
        getByIdHandler = mock(GetWorkItemByIdQueryHandler.class);
        getByProjectHandler = mock(GetWorkItemsByProjectQueryHandler.class);
        controller = new WorkItemController(
                createHandler,
                assignHandler,
                updateHandler,
                updateStatusHandler,
                updateNotesHandler,
                deleteHandler,
                getByIdHandler,
                getByProjectHandler
        );
    }

    @Test
    void createWorkItemReturnsCreatedId() {
        UUID workItemId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        CreateWorkItemCommand command = new CreateWorkItemCommand(
                "Build tests",
                2,
                WorkItemType.Task,
                WorkItemStatus.New,
                null,
                5,
                projectId,
                null
        );
        when(createHandler.handle(command)).thenReturn(workItemId);

        var response = controller.createWorkItem(command);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(workItemId);
    }

    @Test
    void typeHierarchyUsesDomainRulesInEnumOrder() {
        var response = controller.getTypeHierarchy();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry(
                "Epic",
                List.of("Feature", "UserStory", "Task", "Bug", "Subtask")
        );
        assertThat(response.getBody()).containsEntry("Subtask", List.of("Subtask"));
        assertThat(response.getBody().keySet())
                .containsExactly("Epic", "Feature", "UserStory", "Task", "Bug", "Subtask");
    }

    @Test
    void getWorkItemsByProjectDelegatesQuery() {
        UUID projectId = UUID.randomUUID();
        List<WorkItemDto> items = List.of(workItemDto(UUID.randomUUID(), projectId));
        when(getByProjectHandler.handle(new GetWorkItemsByProjectQuery(projectId))).thenReturn(items);

        var response = controller.getWorkItemsByProject(projectId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(items);
    }

    @Test
    void getWorkItemReturnsOkOrNotFound() {
        UUID workItemId = UUID.randomUUID();
        WorkItemDto dto = workItemDto(workItemId, UUID.randomUUID());
        when(getByIdHandler.handle(new GetWorkItemByIdQuery(workItemId))).thenReturn(Optional.of(dto));

        var found = controller.getWorkItem(workItemId);
        assertThat(found.getStatusCode().value()).isEqualTo(200);
        assertThat(found.getBody()).isEqualTo(dto);

        UUID missingId = UUID.randomUUID();
        when(getByIdHandler.handle(new GetWorkItemByIdQuery(missingId))).thenReturn(Optional.empty());

        var missing = controller.getWorkItem(missingId);
        assertThat(missing.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void assignStatusUpdateNotesAndDeleteDelegateCommands() {
        UUID workItemId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        assertThat(controller.assignWorkItem(workItemId, new AssignWorkItemRequest(assigneeId)).getStatusCode().value())
                .isEqualTo(200);
        verify(assignHandler).handle(new AssignWorkItemCommand(workItemId, assigneeId));

        assertThat(controller.updateStatus(workItemId, new UpdateWorkItemStatusRequest(WorkItemStatus.Resolved)).getStatusCode().value())
                .isEqualTo(200);
        verify(updateStatusHandler).handle(new UpdateWorkItemStatusCommand(workItemId, WorkItemStatus.Resolved));

        assertThat(controller.updateNotes(workItemId, new UpdateWorkItemNotesRequest("Updated notes")).getStatusCode().value())
                .isEqualTo(200);
        verify(updateNotesHandler).handle(new UpdateWorkItemNotesCommand(workItemId, "Updated notes"));

        assertThat(controller.deleteWorkItem(workItemId).getStatusCode().value()).isEqualTo(204);
        verify(deleteHandler).handle(new DeleteWorkItemCommand(workItemId));
    }

    @Test
    void updateWorkItemMapsRequestIntoCommand() {
        UUID workItemId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 2, 1, 12, 0);
        UpdateWorkItemRequest request = new UpdateWorkItemRequest(
                "Updated task",
                4,
                WorkItemType.Bug,
                WorkItemStatus.Active,
                dueDate,
                8,
                assigneeId,
                "Updated notes"
        );

        var response = controller.updateWorkItem(workItemId, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        ArgumentCaptor<UpdateWorkItemCommand> captor = ArgumentCaptor.forClass(UpdateWorkItemCommand.class);
        verify(updateHandler).handle(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new UpdateWorkItemCommand(
                workItemId,
                "Updated task",
                4,
                WorkItemType.Bug,
                WorkItemStatus.Active,
                dueDate,
                8,
                assigneeId,
                "Updated notes"
        ));
    }

    private WorkItemDto workItemDto(UUID id, UUID projectId) {
        return new WorkItemDto(
                id,
                1,
                "Task",
                2,
                WorkItemType.Task,
                WorkItemStatus.New,
                null,
                5,
                projectId,
                UUID.randomUUID(),
                null,
                null
        );
    }
}
