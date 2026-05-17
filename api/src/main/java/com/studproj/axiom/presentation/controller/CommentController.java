package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.infrastructure.persistence.entity.CommentEntity;
import com.studproj.axiom.infrastructure.persistence.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    private final CommentJpaRepository commentRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public record CreateCommentReq(String text) {}

    @GetMapping("/work-items/{workItemId}/comments")
    public ResponseEntity<List<CommentEntity>> getComments(@PathVariable UUID workItemId) {
        return ResponseEntity.ok(commentRepository.findByWorkItemIdOrderByCreatedOnAsc(workItemId));
    }

    @PostMapping("/work-items/{workItemId}/comments")
    public ResponseEntity<CommentEntity> addComment(@PathVariable UUID workItemId, @RequestBody CreateCommentReq req) {
        String email = authenticatedUserProvider.getAuthenticatedUserEmail();
        String authorName = userRepository.findByEmail(email)
                .map(u -> u.getUserName())
                .orElse(email);

        CommentEntity comment = CommentEntity.builder()
                .id(UUID.randomUUID())
                .workItemId(workItemId)
                .author(authorName)
                .text(req.text())
                .createdOn(LocalDateTime.now())
                .build();

        CommentEntity saved = commentRepository.save(comment);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
