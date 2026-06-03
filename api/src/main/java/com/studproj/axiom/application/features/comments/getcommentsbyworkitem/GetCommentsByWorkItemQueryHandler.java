package com.studproj.axiom.application.features.comments.getcommentsbyworkitem;

import com.studproj.axiom.application.features.comments.CommentDto;
import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.*;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.infrastructure.persistence.entity.CommentEntity;
import com.studproj.axiom.infrastructure.persistence.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCommentsByWorkItemQueryHandler {
    private final CommentJpaRepository commentRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserRepository userRepository;

    public List<CommentDto> handle(GetCommentsByWorkItemQuery query) {
        WorkItem workItem = workItemRepository.findById(query.workItemId())
                .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        ensureProjectMember(workItem.getProjectId());

        return commentRepository.findByWorkItemIdOrderByCreatedOnAsc(query.workItemId()).stream()
                .map((CommentEntity entity) -> toDto(entity, userRepository))
                .toList();
    }

    private void ensureProjectMember(UUID projectId) {
        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, projectId)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private static CommentDto toDto(CommentEntity entity, UserRepository userRepository) {
        var user = userRepository.findById(entity.getAuthorId())
                .orElse(null);

        String author = user != null
                ? user.getFirstName() + " " + user.getLastName()
                : null;

        return new CommentDto(
                entity.getId(),
                entity.getWorkItemId(),
                entity.getAuthorId(),
                author,
                entity.getText(),
                entity.getCreatedOn()
        );
    }
}
