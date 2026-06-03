package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.comments.CommentDto;
import com.studproj.axiom.application.features.comments.createcomment.CreateCommentCommand;
import com.studproj.axiom.application.features.comments.createcomment.CreateCommentCommandHandler;
import com.studproj.axiom.application.features.comments.deletecomment.DeleteCommentCommand;
import com.studproj.axiom.application.features.comments.deletecomment.DeleteCommentCommandHandler;
import com.studproj.axiom.application.features.comments.getcommentsbyworkitem.GetCommentsByWorkItemQuery;
import com.studproj.axiom.application.features.comments.getcommentsbyworkitem.GetCommentsByWorkItemQueryHandler;
import com.studproj.axiom.presentation.controller.dto.CreateCommentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    private final GetCommentsByWorkItemQueryHandler getCommentsByWorkItemQueryHandler;
    private final CreateCommentCommandHandler createCommentCommandHandler;
    private final DeleteCommentCommandHandler deleteCommentCommandHandler;

    @GetMapping("/work-items/{workItemId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID workItemId) {
        return ResponseEntity.ok(getCommentsByWorkItemQueryHandler.handle(new GetCommentsByWorkItemQuery(workItemId)));
    }

    @PostMapping("/work-items/{workItemId}/comments")
    public ResponseEntity<UUID> addComment(@PathVariable UUID workItemId, @Valid @RequestBody CreateCommentRequest request) {
        UUID id = createCommentCommandHandler.handle(new CreateCommentCommand(workItemId, request.text()));
        return new ResponseEntity<>(id, HttpStatus.CREATED);

    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        deleteCommentCommandHandler.handle(new DeleteCommentCommand(id));
        return ResponseEntity.noContent().build();
    }
}
