package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.Comment;
import com.studproj.axiom.domain.repository.CommentRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaCommentRepository implements CommentRepository {
    private final CommentJpaRepository jpaRepository;

    @Override
    public void save(Comment comment) {
        jpaRepository.save(CommentMapper.toEntity(comment));
    }

    @Override
    public Optional<Comment> findById(UUID id) {
        return jpaRepository.findById(id).map(CommentMapper::toDomain);
    }

    @Override
    public List<Comment> findByWorkItemIdOrderByCreatedOnAsc(UUID workItemId) {
        return jpaRepository.findByWorkItemIdOrderByCreatedOnAsc(workItemId).stream()
                .map(CommentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}