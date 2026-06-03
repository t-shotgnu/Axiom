package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.Comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
    void save(Comment comment);

    Optional<Comment> findById(UUID id);

    List<Comment> findByWorkItemIdOrderByCreatedOnAsc(UUID workItemId);

    void delete(UUID id);
}