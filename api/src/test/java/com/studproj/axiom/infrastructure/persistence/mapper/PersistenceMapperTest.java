package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.Attachment;
import com.studproj.axiom.domain.model.Comment;
import com.studproj.axiom.domain.model.LinkType;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.TaskRelationship;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.infrastructure.persistence.entity.AttachmentEntity;
import com.studproj.axiom.infrastructure.persistence.entity.CommentEntity;
import com.studproj.axiom.infrastructure.persistence.entity.ProjectEntity;
import com.studproj.axiom.infrastructure.persistence.entity.ProjectMembershipEntity;
import com.studproj.axiom.infrastructure.persistence.entity.ProjectRoleEntity;
import com.studproj.axiom.infrastructure.persistence.entity.RefreshTokenEntity;
import com.studproj.axiom.infrastructure.persistence.entity.TaskRelationshipEntity;
import com.studproj.axiom.infrastructure.persistence.entity.UserEntity;
import com.studproj.axiom.infrastructure.persistence.entity.WorkItemEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceMapperTest {

    @Test
    void projectMapperRoundTripsAllFieldsAndHandlesNull() {
        Project domain = new Project(
                UUID.randomUUID(),
                "Axiom",
                "AX",
                "Project management",
                LocalDateTime.of(2026, 1, 1, 12, 0),
                UUID.randomUUID()
        );

        ProjectEntity entity = ProjectMapper.toEntity(domain);
        Project mapped = ProjectMapper.toDomain(entity);

        assertThat(ProjectMapper.toEntity(null)).isNull();
        assertThat(ProjectMapper.toDomain(null)).isNull();
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getName()).isEqualTo(domain.getName());
        assertThat(mapped.getCode()).isEqualTo(domain.getCode());
        assertThat(mapped.getDescription()).isEqualTo(domain.getDescription());
        assertThat(mapped.getCreatedOn()).isEqualTo(domain.getCreatedOn());
        assertThat(mapped.getOwnerId()).isEqualTo(domain.getOwnerId());
    }

    @Test
    void userMapperRoundTripsAllFieldsAndHandlesNull() {
        User domain = User.builder()
                .id(UUID.randomUUID())
                .userName("ada")
                .emailAddress("ada@example.com")
                .password("encoded")
                .firstName("Ada")
                .lastName("Lovelace")
                .dateOfBirth(LocalDate.of(1815, 12, 10))
                .createdOn(LocalDateTime.of(2026, 1, 1, 12, 0))
                .modifiedOn(LocalDateTime.of(2026, 1, 2, 12, 0))
                .active(true)
                .lastLogin(LocalDateTime.of(2026, 1, 3, 12, 0))
                .build();

        UserEntity entity = UserMapper.toEntity(domain);
        User mapped = UserMapper.toDomain(entity);

        assertThat(UserMapper.toEntity(null)).isNull();
        assertThat(UserMapper.toDomain(null)).isNull();
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getUserName()).isEqualTo(domain.getUserName());
        assertThat(mapped.getEmailAddress()).isEqualTo(domain.getEmailAddress());
        assertThat(mapped.getPassword()).isEqualTo(domain.getPassword());
        assertThat(mapped.getFirstName()).isEqualTo(domain.getFirstName());
        assertThat(mapped.getLastName()).isEqualTo(domain.getLastName());
        assertThat(mapped.getDateOfBirth()).isEqualTo(domain.getDateOfBirth());
        assertThat(mapped.getCreatedOn()).isEqualTo(domain.getCreatedOn());
        assertThat(mapped.getModifiedOn()).isEqualTo(domain.getModifiedOn());
        assertThat(mapped.isActive()).isEqualTo(domain.isActive());
        assertThat(mapped.getLastLogin()).isEqualTo(domain.getLastLogin());
    }

    @Test
    void workItemMapperRoundTripsAllFieldsAndHandlesNull() {
        WorkItem domain = WorkItem.builder()
                .id(UUID.randomUUID())
                .controlNo(42)
                .description("Fix issue")
                .priority(2)
                .type(WorkItemType.Bug)
                .status(WorkItemStatus.Active)
                .dueDate(LocalDateTime.of(2026, 2, 1, 12, 0))
                .estimatedEffort(8)
                .projectId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .assigneeId(UUID.randomUUID())
                .notes("Investigate logs")
                .build();

        WorkItemEntity entity = WorkItemMapper.toEntity(domain);
        WorkItem mapped = WorkItemMapper.toDomain(entity);

        assertThat(WorkItemMapper.toEntity(null)).isNull();
        assertThat(WorkItemMapper.toDomain(null)).isNull();
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getControlNo()).isEqualTo(domain.getControlNo());
        assertThat(mapped.getDescription()).isEqualTo(domain.getDescription());
        assertThat(mapped.getPriority()).isEqualTo(domain.getPriority());
        assertThat(mapped.getType()).isEqualTo(domain.getType());
        assertThat(mapped.getStatus()).isEqualTo(domain.getStatus());
        assertThat(mapped.getDueDate()).isEqualTo(domain.getDueDate());
        assertThat(mapped.getEstimatedEffort()).isEqualTo(domain.getEstimatedEffort());
        assertThat(mapped.getProjectId()).isEqualTo(domain.getProjectId());
        assertThat(mapped.getAuthorId()).isEqualTo(domain.getAuthorId());
        assertThat(mapped.getAssigneeId()).isEqualTo(domain.getAssigneeId());
        assertThat(mapped.getNotes()).isEqualTo(domain.getNotes());
    }

    @Test
    void projectMembershipMapperRoundTripsAllFieldsAndHandlesNull() {
        ProjectMembership domain = new ProjectMembership(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );

        ProjectMembershipEntity entity = ProjectMembershipMapper.toEntity(domain);
        ProjectMembership mapped = ProjectMembershipMapper.toDomain(entity);

        assertThat(ProjectMembershipMapper.toEntity(null)).isNull();
        assertThat(ProjectMembershipMapper.toDomain(null)).isNull();
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getProjectId()).isEqualTo(domain.getProjectId());
        assertThat(mapped.getUserId()).isEqualTo(domain.getUserId());
        assertThat(mapped.getRoleId()).isEqualTo(domain.getRoleId());
        assertThat(mapped.getCreatedOn()).isEqualTo(domain.getCreatedOn());
    }

    @Test
    void projectRoleMapperRoundTripsTypeCodesAndHandlesNull() {
        ProjectRole domain = new ProjectRole(UUID.randomUUID(), ProjectRoleType.ADMIN);

        ProjectRoleEntity entity = ProjectRoleMapper.toEntity(domain);
        ProjectRole mapped = ProjectRoleMapper.toDomain(entity);

        assertThat(ProjectRoleMapper.toEntity(null)).isNull();
        assertThat(ProjectRoleMapper.toDomain(null)).isNull();
        assertThat(entity.getCode()).isEqualTo("ADMIN");
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getType()).isEqualTo(ProjectRoleType.ADMIN);
    }

    @Test
    void refreshTokenMapperRoundTripsAllFieldsAndHandlesNull() {
        RefreshToken domain = new RefreshToken(
                UUID.randomUUID(),
                "refresh-token",
                UUID.randomUUID(),
                true,
                LocalDateTime.of(2026, 1, 10, 12, 0),
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );

        RefreshTokenEntity entity = RefreshTokenMapper.toEntity(domain);
        RefreshToken mapped = RefreshTokenMapper.toDomain(entity);

        assertThat(RefreshTokenMapper.toEntity(null)).isNull();
        assertThat(RefreshTokenMapper.toDomain(null)).isNull();
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getToken()).isEqualTo(domain.getToken());
        assertThat(mapped.getUserId()).isEqualTo(domain.getUserId());
        assertThat(mapped.isRevoked()).isEqualTo(domain.isRevoked());
        assertThat(mapped.getExpiresAt()).isEqualTo(domain.getExpiresAt());
        assertThat(mapped.getCreatedAt()).isEqualTo(domain.getCreatedAt());
    }

    @Test
    void commentMapperRoundTripsAllFieldsAndHandlesNull() {
        Comment domain = new Comment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Looks good",
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );

        CommentEntity entity = CommentMapper.toEntity(domain);
        Comment mapped = CommentMapper.toDomain(entity);

        assertThat(CommentMapper.toEntity(null)).isNull();
        assertThat(CommentMapper.toDomain(null)).isNull();
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getWorkItemId()).isEqualTo(domain.getWorkItemId());
        assertThat(mapped.getAuthorId()).isEqualTo(domain.getAuthorId());
        assertThat(mapped.getText()).isEqualTo(domain.getText());
        assertThat(mapped.getCreatedOn()).isEqualTo(domain.getCreatedOn());
    }

    @Test
    void taskRelationshipMapperRoundTripsAllFieldsAndHandlesNull() {
        TaskRelationship domain = TaskRelationship.builder()
                .id(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .targetId(UUID.randomUUID())
                .linkType(LinkType.ParentOf)
                .build();

        TaskRelationshipEntity entity = TaskRelationshipMapper.toEntity(domain);
        TaskRelationship mapped = TaskRelationshipMapper.toDomain(entity);

        assertThat(TaskRelationshipMapper.toEntity(null)).isNull();
        assertThat(TaskRelationshipMapper.toDomain(null)).isNull();
        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getSourceId()).isEqualTo(domain.getSourceId());
        assertThat(mapped.getTargetId()).isEqualTo(domain.getTargetId());
        assertThat(mapped.getLinkType()).isEqualTo(domain.getLinkType());
    }

    @Test
    void attachmentMapperRoundTripsAllFields() {
        Attachment domain = new Attachment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "objects/report.pdf",
                "report.pdf",
                2048L,
                "application/pdf",
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );

        AttachmentEntity entity = AttachmentMapper.toEntity(domain);
        Attachment mapped = AttachmentMapper.toDomain(entity);

        assertThat(mapped.getId()).isEqualTo(domain.getId());
        assertThat(mapped.getWorkItemId()).isEqualTo(domain.getWorkItemId());
        assertThat(mapped.getUploadedBy()).isEqualTo(domain.getUploadedBy());
        assertThat(mapped.getObjectKey()).isEqualTo(domain.getObjectKey());
        assertThat(mapped.getFileName()).isEqualTo(domain.getFileName());
        assertThat(mapped.getFileSize()).isEqualTo(domain.getFileSize());
        assertThat(mapped.getFileType()).isEqualTo(domain.getFileType());
        assertThat(mapped.getUploadedOn()).isEqualTo(domain.getUploadedOn());
    }
}
