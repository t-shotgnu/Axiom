package com.studproj.axiom.application.features.comments.createcomment;

import com.studproj.axiom.application.features.comments.CommentDto;
import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.Comment;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.CommentRepository;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCommentCommandHandler {
    private final CommentRepository commentRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public UUID handle(CreateCommentCommand command) {
        WorkItem workItem = workItemRepository.findById(command.workItemId())
                .orElseThrow(() -> new NotFoundException("WorkItem not found"));

        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, workItem.getProjectId())) {
            throw new ForbiddenException("You are not a member of this project");
        }

        var currentUserId = authenticatedUserProvider.getAuthenticatedUserId();

        Comment comment = new Comment(
                UUID.randomUUID(),
                command.workItemId(),
                currentUserId,
                command.text(),
                LocalDateTime.now()
        );

        commentRepository.save(comment);

        return comment.getId();
    }
}
