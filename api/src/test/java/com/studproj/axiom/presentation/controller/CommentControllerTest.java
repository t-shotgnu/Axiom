package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.comments.CommentDto;
import com.studproj.axiom.application.features.comments.createcomment.CreateCommentCommand;
import com.studproj.axiom.application.features.comments.createcomment.CreateCommentCommandHandler;
import com.studproj.axiom.application.features.comments.deletecomment.DeleteCommentCommand;
import com.studproj.axiom.application.features.comments.deletecomment.DeleteCommentCommandHandler;
import com.studproj.axiom.application.features.comments.getcommentsbyworkitem.GetCommentsByWorkItemQuery;
import com.studproj.axiom.application.features.comments.getcommentsbyworkitem.GetCommentsByWorkItemQueryHandler;
import com.studproj.axiom.presentation.controller.dto.CreateCommentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentControllerTest {

    private GetCommentsByWorkItemQueryHandler getHandler;
    private CreateCommentCommandHandler createHandler;
    private DeleteCommentCommandHandler deleteHandler;
    private CommentController controller;

    @BeforeEach
    void setUp() {
        getHandler = mock(GetCommentsByWorkItemQueryHandler.class);
        createHandler = mock(CreateCommentCommandHandler.class);
        deleteHandler = mock(DeleteCommentCommandHandler.class);
        controller = new CommentController(getHandler, createHandler, deleteHandler);
    }

    @Test
    void getCommentsDelegatesQuery() {
        UUID workItemId = UUID.randomUUID();
        List<CommentDto> comments = List.of(new CommentDto(
                UUID.randomUUID(),
                workItemId,
                UUID.randomUUID(),
                "Ada",
                "Looks good",
                LocalDateTime.of(2026, 1, 1, 12, 0)
        ));
        when(getHandler.handle(new GetCommentsByWorkItemQuery(workItemId))).thenReturn(comments);

        var response = controller.getComments(workItemId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(comments);
    }

    @Test
    void addCommentMapsRequestAndReturnsCreatedId() {
        UUID workItemId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest("Ship it");
        when(createHandler.handle(new CreateCommentCommand(workItemId, "Ship it"))).thenReturn(commentId);

        var response = controller.addComment(workItemId, request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(commentId);
    }

    @Test
    void deleteCommentDelegatesCommand() {
        UUID commentId = UUID.randomUUID();

        var response = controller.deleteComment(commentId);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(deleteHandler).handle(new DeleteCommentCommand(commentId));
    }
}
