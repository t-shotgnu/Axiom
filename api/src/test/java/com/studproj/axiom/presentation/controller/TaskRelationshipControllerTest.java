package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.taskrelationships.TaskRelationshipDto;
import com.studproj.axiom.application.features.taskrelationships.createtaskrelationship.CreateTaskRelationshipCommand;
import com.studproj.axiom.application.features.taskrelationships.createtaskrelationship.CreateTaskRelationshipCommandHandler;
import com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship.DeleteTaskRelationshipCommand;
import com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship.DeleteTaskRelationshipCommandHandler;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByProjectQuery;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByProjectQueryHandler;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByWorkItemQuery;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByWorkItemQueryHandler;
import com.studproj.axiom.domain.model.LinkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskRelationshipControllerTest {

    private CreateTaskRelationshipCommandHandler createHandler;
    private DeleteTaskRelationshipCommandHandler deleteHandler;
    private GetTaskRelationshipsByProjectQueryHandler getByProjectHandler;
    private GetTaskRelationshipsByWorkItemQueryHandler getByWorkItemHandler;
    private TaskRelationshipController controller;

    @BeforeEach
    void setUp() {
        createHandler = mock(CreateTaskRelationshipCommandHandler.class);
        deleteHandler = mock(DeleteTaskRelationshipCommandHandler.class);
        getByProjectHandler = mock(GetTaskRelationshipsByProjectQueryHandler.class);
        getByWorkItemHandler = mock(GetTaskRelationshipsByWorkItemQueryHandler.class);
        controller = new TaskRelationshipController(createHandler, deleteHandler, getByProjectHandler, getByWorkItemHandler);
    }

    @Test
    void createRelationshipReturnsCreatedId() {
        UUID relationshipId = UUID.randomUUID();
        CreateTaskRelationshipCommand command = new CreateTaskRelationshipCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LinkType.Blocks
        );
        when(createHandler.handle(command)).thenReturn(relationshipId);

        var response = controller.createRelationship(command);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(relationshipId);
    }

    @Test
    void deleteRelationshipDelegatesCommand() {
        UUID relationshipId = UUID.randomUUID();

        var response = controller.deleteRelationship(relationshipId);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(deleteHandler).handle(new DeleteTaskRelationshipCommand(relationshipId));
    }

    @Test
    void getRelationshipsByProjectDelegatesQuery() {
        UUID projectId = UUID.randomUUID();
        List<TaskRelationshipDto> dtos = List.of(dto());
        when(getByProjectHandler.handle(new GetTaskRelationshipsByProjectQuery(projectId))).thenReturn(dtos);

        var response = controller.getRelationshipsByProject(projectId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(dtos);
    }

    @Test
    void getRelationshipsByWorkItemDelegatesQuery() {
        UUID workItemId = UUID.randomUUID();
        List<TaskRelationshipDto> dtos = List.of(dto());
        when(getByWorkItemHandler.handle(new GetTaskRelationshipsByWorkItemQuery(workItemId))).thenReturn(dtos);

        var response = controller.getRelationshipsByWorkItem(workItemId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(dtos);
    }

    private TaskRelationshipDto dto() {
        return new TaskRelationshipDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LinkType.RelatesTo);
    }
}
