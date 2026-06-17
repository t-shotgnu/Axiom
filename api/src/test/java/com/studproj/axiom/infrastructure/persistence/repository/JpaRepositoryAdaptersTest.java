package com.studproj.axiom.infrastructure.persistence.repository;

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
import com.studproj.axiom.infrastructure.persistence.mapper.AttachmentMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.CommentMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.ProjectMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.ProjectMembershipMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.ProjectRoleMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.TaskRelationshipMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.UserMapper;
import com.studproj.axiom.infrastructure.persistence.mapper.WorkItemMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaRepositoryAdaptersTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Test
    void attachmentRepositoryMapsSavesQueriesAndDeletes() {
        AttachmentJpaRepository jpa = mock(AttachmentJpaRepository.class);
        JpaAttachmentRepository repository = new JpaAttachmentRepository(jpa);
        Attachment attachment = attachment();
        AttachmentEntity entity = AttachmentMapper.toEntity(attachment);
        when(jpa.findById(attachment.getId())).thenReturn(Optional.of(entity));
        when(jpa.findByWorkItemIdOrderByUploadedOnDesc(attachment.getWorkItemId())).thenReturn(List.of(entity));

        repository.save(attachment);
        Optional<Attachment> found = repository.findById(attachment.getId());
        List<Attachment> byWorkItem = repository.findByWorkItemIdOrderByUploadedOnDesc(attachment.getWorkItemId());
        repository.delete(attachment.getId());

        ArgumentCaptor<AttachmentEntity> saved = ArgumentCaptor.forClass(AttachmentEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getObjectKey()).isEqualTo(attachment.getObjectKey());
        assertThat(found).hasValueSatisfying(value -> assertThat(value.getFileName()).isEqualTo(attachment.getFileName()));
        assertThat(byWorkItem).extracting(Attachment::getId).containsExactly(attachment.getId());
        verify(jpa).deleteById(attachment.getId());
    }

    @Test
    void commentRepositoryMapsSavesQueriesAndDeletes() {
        CommentJpaRepository jpa = mock(CommentJpaRepository.class);
        JpaCommentRepository repository = new JpaCommentRepository(jpa);
        Comment comment = comment();
        CommentEntity entity = CommentMapper.toEntity(comment);
        when(jpa.findById(comment.getId())).thenReturn(Optional.of(entity));
        when(jpa.findByWorkItemIdOrderByCreatedOnAsc(comment.getWorkItemId())).thenReturn(List.of(entity));

        repository.save(comment);
        Optional<Comment> found = repository.findById(comment.getId());
        List<Comment> byWorkItem = repository.findByWorkItemIdOrderByCreatedOnAsc(comment.getWorkItemId());
        repository.delete(comment.getId());

        ArgumentCaptor<CommentEntity> saved = ArgumentCaptor.forClass(CommentEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getText()).isEqualTo(comment.getText());
        assertThat(found).hasValueSatisfying(value -> assertThat(value.getAuthorId()).isEqualTo(comment.getAuthorId()));
        assertThat(byWorkItem).extracting(Comment::getId).containsExactly(comment.getId());
        verify(jpa).deleteById(comment.getId());
    }

    @Test
    void projectRepositoryMapsSavesQueriesCountsAndDeletes() {
        ProjectJpaRepository jpa = mock(ProjectJpaRepository.class);
        JpaProjectRepository repository = new JpaProjectRepository(jpa);
        Project project = project();
        ProjectEntity entity = ProjectMapper.toEntity(project);
        when(jpa.findById(project.getId())).thenReturn(Optional.of(entity));
        when(jpa.findByIdIn(List.of(project.getId()))).thenReturn(List.of(entity));
        when(jpa.findAll()).thenReturn(List.of(entity));
        when(jpa.count()).thenReturn(7L);
        when(jpa.findByCode("AX")).thenReturn(List.of(entity));

        repository.save(project);
        Optional<Project> found = repository.findById(project.getId());
        List<Project> byIds = repository.findByIds(List.of(project.getId()));
        List<Project> all = repository.findAll();
        long count = repository.countAll();
        Optional<Project> byCode = repository.findByCode("AX");
        repository.delete(project.getId());

        ArgumentCaptor<ProjectEntity> saved = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getCode()).isEqualTo("AX");
        assertThat(found).hasValueSatisfying(value -> assertThat(value.getName()).isEqualTo("Axiom"));
        assertThat(byIds).extracting(Project::getId).containsExactly(project.getId());
        assertThat(all).extracting(Project::getId).containsExactly(project.getId());
        assertThat(count).isEqualTo(7L);
        assertThat(byCode).hasValueSatisfying(value -> assertThat(value.getCode()).isEqualTo("AX"));
        verify(jpa).deleteById(project.getId());
    }

    @Test
    void projectRepositoryShortCircuitsEmptyIdCollections() {
        ProjectJpaRepository jpa = mock(ProjectJpaRepository.class);
        JpaProjectRepository repository = new JpaProjectRepository(jpa);

        assertThat(repository.findByIds(null)).isEmpty();
        assertThat(repository.findByIds(List.of())).isEmpty();

        verify(jpa, never()).findByIdIn(any());
    }

    @Test
    void projectMembershipRepositoryDelegatesMembershipSpecificQueries() {
        ProjectMembershipJpaRepository jpa = mock(ProjectMembershipJpaRepository.class);
        JpaProjectMembershipRepository repository = new JpaProjectMembershipRepository(jpa);
        ProjectMembership membership = projectMembership();
        ProjectMembershipEntity entity = ProjectMembershipMapper.toEntity(membership);
        when(jpa.findByProjectIdAndUserId(membership.getProjectId(), membership.getUserId())).thenReturn(Optional.of(entity));
        when(jpa.findByProjectId(membership.getProjectId())).thenReturn(List.of(entity));
        when(jpa.findByUserId(membership.getUserId())).thenReturn(List.of(entity));
        when(jpa.existsByProjectIdAndUserId(membership.getProjectId(), membership.getUserId())).thenReturn(true);

        repository.save(membership);
        Optional<ProjectMembership> found = repository.findByProjectIdAndUserId(membership.getProjectId(), membership.getUserId());
        List<ProjectMembership> byProject = repository.findByProjectId(membership.getProjectId());
        List<ProjectMembership> byUser = repository.findByUserId(membership.getUserId());
        boolean exists = repository.existsByProjectIdAndUserId(membership.getProjectId(), membership.getUserId());
        repository.deleteByProjectIdAndUserId(membership.getProjectId(), membership.getUserId());
        repository.deleteByProjectId(membership.getProjectId());

        ArgumentCaptor<ProjectMembershipEntity> saved = ArgumentCaptor.forClass(ProjectMembershipEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getRoleId()).isEqualTo(membership.getRoleId());
        assertThat(found).hasValueSatisfying(value -> assertThat(value.getUserId()).isEqualTo(membership.getUserId()));
        assertThat(byProject).extracting(ProjectMembership::getId).containsExactly(membership.getId());
        assertThat(byUser).extracting(ProjectMembership::getId).containsExactly(membership.getId());
        assertThat(exists).isTrue();
        verify(jpa).deleteByProjectIdAndUserId(membership.getProjectId(), membership.getUserId());
        verify(jpa).deleteByProjectId(membership.getProjectId());
    }

    @Test
    void projectRoleRepositoryMapsSingleAndBulkOperations() {
        ProjectRoleJpaRepository jpa = mock(ProjectRoleJpaRepository.class);
        JpaProjectRoleRepository repository = new JpaProjectRoleRepository(jpa);
        ProjectRole admin = new ProjectRole(UUID.randomUUID(), ProjectRoleType.ADMIN);
        ProjectRole member = new ProjectRole(UUID.randomUUID(), ProjectRoleType.MEMBER);
        ProjectRoleEntity adminEntity = ProjectRoleMapper.toEntity(admin);
        ProjectRoleEntity memberEntity = ProjectRoleMapper.toEntity(member);
        when(jpa.findByCode("ADMIN")).thenReturn(Optional.of(adminEntity));
        when(jpa.findById(admin.getId())).thenReturn(Optional.of(adminEntity));
        when(jpa.findAll()).thenReturn(List.of(adminEntity, memberEntity));

        repository.save(admin);
        repository.saveAll(List.of(admin, member));
        Optional<ProjectRole> byType = repository.findByType(ProjectRoleType.ADMIN);
        Optional<ProjectRole> byId = repository.findById(admin.getId());
        List<ProjectRole> all = repository.findAll();

        ArgumentCaptor<ProjectRoleEntity> saved = ArgumentCaptor.forClass(ProjectRoleEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getCode()).isEqualTo("ADMIN");

        ArgumentCaptor<Iterable<ProjectRoleEntity>> savedAll = ArgumentCaptor.forClass(Iterable.class);
        verify(jpa).saveAll(savedAll.capture());
        assertThat(savedAll.getValue()).extracting(ProjectRoleEntity::getCode).containsExactly("ADMIN", "MEMBER");
        assertThat(byType).hasValueSatisfying(value -> assertThat(value.getType()).isEqualTo(ProjectRoleType.ADMIN));
        assertThat(byId).hasValueSatisfying(value -> assertThat(value.getId()).isEqualTo(admin.getId()));
        assertThat(all).extracting(ProjectRole::getType).containsExactly(ProjectRoleType.ADMIN, ProjectRoleType.MEMBER);
    }

    @Test
    void refreshTokenRepositoryMapsSingleAndBulkOperations() {
        RefreshTokenJpaRepository jpa = mock(RefreshTokenJpaRepository.class);
        JpaRefreshTokenRepository repository = new JpaRefreshTokenRepository(jpa);
        RefreshToken token = refreshToken();
        RefreshTokenEntity entity = RefreshTokenMapper.toEntity(token);
        when(jpa.findByToken("refresh")).thenReturn(Optional.of(entity));
        when(jpa.findByUserId(token.getUserId())).thenReturn(List.of(entity));

        repository.save(token);
        Optional<RefreshToken> byToken = repository.findByToken("refresh");
        List<RefreshToken> byUser = repository.findByUserId(token.getUserId());
        repository.saveAll(List.of(token));

        ArgumentCaptor<RefreshTokenEntity> saved = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getToken()).isEqualTo("refresh");

        ArgumentCaptor<Iterable<RefreshTokenEntity>> savedAll = ArgumentCaptor.forClass(Iterable.class);
        verify(jpa).saveAll(savedAll.capture());
        assertThat(savedAll.getValue()).extracting(RefreshTokenEntity::getToken).containsExactly("refresh");
        assertThat(byToken).hasValueSatisfying(value -> assertThat(value.getUserId()).isEqualTo(token.getUserId()));
        assertThat(byUser).extracting(RefreshToken::getToken).containsExactly("refresh");
    }

    @Test
    void taskRelationshipRepositoryDelegatesAllRelationshipQueries() {
        TaskRelationshipJpaRepository jpa = mock(TaskRelationshipJpaRepository.class);
        JpaTaskRelationshipRepository repository = new JpaTaskRelationshipRepository(jpa);
        TaskRelationship relationship = taskRelationship();
        TaskRelationshipEntity entity = TaskRelationshipMapper.toEntity(relationship);
        UUID projectId = UUID.randomUUID();
        when(jpa.findById(relationship.getId())).thenReturn(Optional.of(entity));
        when(jpa.findByProjectId(projectId)).thenReturn(List.of(entity));
        when(jpa.findBySourceId(relationship.getSourceId())).thenReturn(List.of(entity));
        when(jpa.findByTargetId(relationship.getTargetId())).thenReturn(List.of(entity));
        when(jpa.findBySourceIdAndLinkType(relationship.getSourceId(), LinkType.Blocks)).thenReturn(List.of(entity));
        when(jpa.findByTargetIdAndLinkType(relationship.getTargetId(), LinkType.Blocks)).thenReturn(List.of(entity));
        when(jpa.findBySourceIdAndTargetIdAndLinkType(relationship.getSourceId(), relationship.getTargetId(), LinkType.Blocks))
                .thenReturn(Optional.of(entity));

        repository.save(relationship);
        Optional<TaskRelationship> found = repository.findById(relationship.getId());
        List<TaskRelationship> byProject = repository.findByProjectId(projectId);
        List<TaskRelationship> bySource = repository.findBySourceId(relationship.getSourceId());
        List<TaskRelationship> byTarget = repository.findByTargetId(relationship.getTargetId());
        List<TaskRelationship> bySourceAndType = repository.findBySourceIdAndLinkType(relationship.getSourceId(), LinkType.Blocks);
        List<TaskRelationship> byTargetAndType = repository.findByTargetIdAndLinkType(relationship.getTargetId(), LinkType.Blocks);
        Optional<TaskRelationship> exact = repository.findBySourceIdAndTargetIdAndLinkType(
                relationship.getSourceId(),
                relationship.getTargetId(),
                LinkType.Blocks
        );
        repository.delete(relationship.getId());

        ArgumentCaptor<TaskRelationshipEntity> saved = ArgumentCaptor.forClass(TaskRelationshipEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getLinkType()).isEqualTo(LinkType.Blocks);
        assertThat(found).hasValueSatisfying(value -> assertThat(value.getTargetId()).isEqualTo(relationship.getTargetId()));
        assertThat(byProject).extracting(TaskRelationship::getId).containsExactly(relationship.getId());
        assertThat(bySource).extracting(TaskRelationship::getId).containsExactly(relationship.getId());
        assertThat(byTarget).extracting(TaskRelationship::getId).containsExactly(relationship.getId());
        assertThat(bySourceAndType).extracting(TaskRelationship::getId).containsExactly(relationship.getId());
        assertThat(byTargetAndType).extracting(TaskRelationship::getId).containsExactly(relationship.getId());
        assertThat(exact).hasValueSatisfying(value -> assertThat(value.getLinkType()).isEqualTo(LinkType.Blocks));
        verify(jpa).deleteById(relationship.getId());
    }

    @Test
    void userRepositoryMapsSavesLookupsAndDeletes() {
        UserJpaRepository jpa = mock(UserJpaRepository.class);
        JpaUserRepository repository = new JpaUserRepository(jpa);
        User user = user();
        UserEntity entity = UserMapper.toEntity(user);
        when(jpa.findById(user.getId())).thenReturn(Optional.of(entity));
        when(jpa.findByEmailAddress(user.getEmailAddress())).thenReturn(Optional.of(entity));

        repository.save(user);
        Optional<User> byId = repository.findById(user.getId());
        Optional<User> byEmail = repository.findByEmail(user.getEmailAddress());
        repository.delete(user.getId());

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getEmailAddress()).isEqualTo("ada@example.com");
        assertThat(byId).hasValueSatisfying(value -> assertThat(value.getUserName()).isEqualTo("ada"));
        assertThat(byEmail).hasValueSatisfying(value -> assertThat(value.getId()).isEqualTo(user.getId()));
        verify(jpa).deleteById(user.getId());
    }

    @Test
    void workItemRepositoryMapsQueriesCountsPagingAndDeletes() {
        WorkItemJpaRepository jpa = mock(WorkItemJpaRepository.class);
        JpaWorkItemRepository repository = new JpaWorkItemRepository(jpa);
        WorkItem workItem = workItem();
        WorkItemEntity entity = WorkItemMapper.toEntity(workItem);
        List<WorkItemStatus> statuses = List.of(WorkItemStatus.New, WorkItemStatus.Active);
        when(jpa.findById(workItem.getId())).thenReturn(Optional.of(entity));
        when(jpa.findByProjectId(workItem.getProjectId())).thenReturn(List.of(entity));
        when(jpa.countByStatusIn(statuses)).thenReturn(11L);
        when(jpa.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(jpa.findMaxControlNoByProjectId(workItem.getProjectId())).thenReturn(Optional.of(42));

        repository.save(workItem);
        Optional<WorkItem> byId = repository.findById(workItem.getId());
        List<WorkItem> byProject = repository.findByProjectId(workItem.getProjectId());
        long count = repository.countByStatusIn(statuses);
        List<WorkItem> recent = repository.findRecent(5);
        Optional<Integer> maxControlNo = repository.findMaxControlNoByProjectId(workItem.getProjectId());
        repository.delete(workItem.getId());

        ArgumentCaptor<WorkItemEntity> saved = ArgumentCaptor.forClass(WorkItemEntity.class);
        verify(jpa).save(saved.capture());
        assertThat(saved.getValue().getControlNo()).isEqualTo(42);
        assertThat(byId).hasValueSatisfying(value -> assertThat(value.getDescription()).isEqualTo("Fix issue"));
        assertThat(byProject).extracting(WorkItem::getId).containsExactly(workItem.getId());
        assertThat(count).isEqualTo(11L);
        assertThat(recent).extracting(WorkItem::getId).containsExactly(workItem.getId());
        assertThat(maxControlNo).contains(42);
        verify(jpa).deleteById(workItem.getId());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(jpa).findAll(pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().getOrderFor("id").getDirection().isDescending()).isTrue();
    }

    private Attachment attachment() {
        return new Attachment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "objects/report.pdf",
                "report.pdf",
                2048L,
                "application/pdf",
                now
        );
    }

    private Comment comment() {
        return new Comment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Looks good", now);
    }

    private Project project() {
        return new Project(UUID.randomUUID(), "Axiom", "AX", "Planning", now, UUID.randomUUID());
    }

    private ProjectMembership projectMembership() {
        return new ProjectMembership(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), now);
    }

    private RefreshToken refreshToken() {
        return new RefreshToken(UUID.randomUUID(), "refresh", UUID.randomUUID(), false, now.plusDays(7), now);
    }

    private TaskRelationship taskRelationship() {
        return TaskRelationship.builder()
                .id(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .targetId(UUID.randomUUID())
                .linkType(LinkType.Blocks)
                .build();
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .userName("ada")
                .emailAddress("ada@example.com")
                .password("encoded")
                .firstName("Ada")
                .lastName("Lovelace")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .createdOn(now)
                .modifiedOn(now.plusDays(1))
                .active(true)
                .lastLogin(now.plusDays(2))
                .build();
    }

    private WorkItem workItem() {
        return WorkItem.builder()
                .id(UUID.randomUUID())
                .controlNo(42)
                .description("Fix issue")
                .priority(2)
                .type(WorkItemType.Bug)
                .status(WorkItemStatus.Active)
                .dueDate(now.plusDays(10))
                .estimatedEffort(8)
                .projectId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .assigneeId(UUID.randomUUID())
                .notes("Investigate logs")
                .build();
    }
}
