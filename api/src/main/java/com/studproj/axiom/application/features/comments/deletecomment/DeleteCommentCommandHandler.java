package com.studproj.axiom.application.features.comments.deletecomment;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.infrastructure.persistence.entity.CommentEntity;
import com.studproj.axiom.infrastructure.persistence.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCommentCommandHandler {
    private final CommentJpaRepository commentRepository;
    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(DeleteCommentCommand command) {
        CommentEntity comment = commentRepository.findById(command.id())
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        WorkItem workItem = workItemRepository.findById(comment.getWorkItemId())
                .orElseThrow(() -> new EntityNotFoundException("WorkItem not found"));

        ensureProjectMember(workItem.getProjectId());

        if (comment.getAuthorId() != null && comment.getAuthorId().equals(authenticatedUserProvider.getAuthenticatedUserId())) {
            commentRepository.deleteById(command.id());
            return;
        }

        if (ProjectAccessChecks.isProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                workItem.getProjectId())) {
            commentRepository.deleteById(command.id());
            return;
        }

        throw new AccessDeniedException("Only the author or project admin can delete this comment");
    }

    private void ensureProjectMember(java.util.UUID projectId) {
        if (!ProjectAccessChecks.isProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, projectId)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }
}
