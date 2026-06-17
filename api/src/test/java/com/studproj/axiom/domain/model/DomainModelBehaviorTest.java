package com.studproj.axiom.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainModelBehaviorTest {

    @Test
    void projectExposesConstructorValues() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        LocalDateTime createdOn = LocalDateTime.of(2026, 1, 1, 12, 0);

        Project project = new Project(id, "Axiom", "AX", "Project management", createdOn, ownerId);

        assertThat(project.getId()).isEqualTo(id);
        assertThat(project.getName()).isEqualTo("Axiom");
        assertThat(project.getCode()).isEqualTo("AX");
        assertThat(project.getDescription()).isEqualTo("Project management");
        assertThat(project.getCreatedOn()).isEqualTo(createdOn);
        assertThat(project.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void projectMembershipChangesRoleAndRejectsNullRoles() {
        UUID oldRoleId = UUID.randomUUID();
        UUID newRoleId = UUID.randomUUID();
        ProjectMembership membership = new ProjectMembership(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                oldRoleId,
                LocalDateTime.now()
        );

        membership.changeRole(newRoleId);

        assertThat(membership.getRoleId()).isEqualTo(newRoleId);
        assertThatThrownBy(() -> membership.changeRole(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role ID cannot be null");
    }

    @Test
    void refreshTokenReportsExpirationAndRevocation() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        RefreshToken expiredToken = new RefreshToken(
                UUID.randomUUID(),
                "expired",
                UUID.randomUUID(),
                false,
                now.minusSeconds(1),
                now.minusDays(1)
        );
        RefreshToken activeToken = new RefreshToken(
                UUID.randomUUID(),
                "active",
                UUID.randomUUID(),
                false,
                now.plusSeconds(1),
                now.minusDays(1)
        );

        assertThat(expiredToken.isExpired(now)).isTrue();
        assertThat(activeToken.isExpired(now)).isFalse();

        activeToken.revoke();
        assertThat(activeToken.isRevoked()).isTrue();
    }

    @Test
    void userLifecycleMethodsUpdateStateAndModificationTimes() {
        LocalDateTime createdOn = LocalDateTime.of(2026, 1, 1, 12, 0);
        User user = User.builder()
                .id(UUID.randomUUID())
                .userName("ada")
                .emailAddress("ada@example.com")
                .password("old-password")
                .createdOn(createdOn)
                .active(false)
                .build();

        user.activate();
        LocalDateTime activatedAt = user.getModifiedOn();
        user.updateProfile("Ada", "Lovelace", LocalDate.of(1815, 12, 10));
        LocalDateTime profileUpdatedAt = user.getModifiedOn();
        user.changePassword("new-password");
        user.updateLoginTime();

        assertThat(user.isActive()).isTrue();
        assertThat(activatedAt).isNotNull();
        assertThat(profileUpdatedAt).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Ada");
        assertThat(user.getLastName()).isEqualTo("Lovelace");
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1815, 12, 10));
        assertThat(user.getPassword()).isEqualTo("new-password");
        assertThat(user.getLastLogin()).isNotNull();

        user.deactivate();
        assertThat(user.isActive()).isFalse();
        assertThat(user.getModifiedOn()).isNotNull();
    }

    @Test
    void workItemUpdatesDetailsAndNotes() {
        UUID assigneeId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 2, 1, 12, 0);
        WorkItem workItem = WorkItem.builder()
                .description("Old")
                .priority(1)
                .type(WorkItemType.Task)
                .status(WorkItemStatus.New)
                .notes("Old notes")
                .build();

        workItem.updateDetails("New", 4, WorkItemType.Bug, dueDate, 13);
        workItem.updateNotes("New notes");
        workItem.assignTo(assigneeId);
        workItem.updateStatus(WorkItemStatus.InTesting);

        assertThat(workItem.getDescription()).isEqualTo("New");
        assertThat(workItem.getPriority()).isEqualTo(4);
        assertThat(workItem.getType()).isEqualTo(WorkItemType.Bug);
        assertThat(workItem.getDueDate()).isEqualTo(dueDate);
        assertThat(workItem.getEstimatedEffort()).isEqualTo(13);
        assertThat(workItem.getNotes()).isEqualTo("New notes");
        assertThat(workItem.getAssigneeId()).isEqualTo(assigneeId);
        assertThat(workItem.getStatus()).isEqualTo(WorkItemStatus.InTesting);
    }

    @Test
    void workItemTypeHierarchyMatchesDomainRules() {
        assertThat(WorkItemType.Epic.allowedChildTypes())
                .containsExactly(WorkItemType.Feature, WorkItemType.UserStory, WorkItemType.Task, WorkItemType.Bug, WorkItemType.Subtask);
        assertThat(WorkItemType.Feature.allowedChildTypes())
                .containsExactly(WorkItemType.UserStory, WorkItemType.Task, WorkItemType.Bug, WorkItemType.Subtask);
        assertThat(WorkItemType.UserStory.allowedChildTypes())
                .containsExactly(WorkItemType.Task, WorkItemType.Bug, WorkItemType.Subtask);
        assertThat(WorkItemType.Task.allowedChildTypes())
                .containsExactly(WorkItemType.Subtask);
        assertThat(WorkItemType.Bug.allowedChildTypes())
                .containsExactly(WorkItemType.Subtask);
        assertThat(WorkItemType.Subtask.allowedChildTypes())
                .containsExactly(WorkItemType.Subtask);

        assertThat(List.of(WorkItemType.values()))
                .extracting(WorkItemType::level)
                .containsExactly(1, 2, 3, 4, 4, 5);
    }

    @Test
    void attachmentCommentAndRelationshipExposeConstructorValues() {
        UUID id = UUID.randomUUID();
        UUID workItemId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdOn = LocalDateTime.of(2026, 1, 1, 12, 0);

        Attachment attachment = new Attachment(id, workItemId, userId, "objects/file.txt", "file.txt", 42L, "text/plain", createdOn);
        Comment comment = new Comment(id, workItemId, userId, "Looks good", createdOn);
        TaskRelationship relationship = TaskRelationship.builder()
                .id(id)
                .sourceId(workItemId)
                .targetId(UUID.randomUUID())
                .linkType(LinkType.BlockedBy)
                .build();

        assertThat(attachment.getObjectKey()).isEqualTo("objects/file.txt");
        assertThat(attachment.getFileName()).isEqualTo("file.txt");
        assertThat(attachment.getFileSize()).isEqualTo(42L);
        assertThat(attachment.getFileType()).isEqualTo("text/plain");
        assertThat(attachment.getUploadedOn()).isEqualTo(createdOn);

        assertThat(comment.getText()).isEqualTo("Looks good");
        assertThat(comment.getCreatedOn()).isEqualTo(createdOn);

        assertThat(relationship.getSourceId()).isEqualTo(workItemId);
        assertThat(relationship.getLinkType()).isEqualTo(LinkType.BlockedBy);
    }
}
