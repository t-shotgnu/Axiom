package com.studproj.axiom.application.features.attachments;

import com.studproj.axiom.application.features.attachments.deleteattachment.DeleteAttachmentCommand;
import com.studproj.axiom.application.features.attachments.deleteattachment.DeleteAttachmentCommandHandler;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentQuery;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentQueryHandler;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentResult;
import com.studproj.axiom.application.features.attachments.uploadattachment.UploadAttachmentCommand;
import com.studproj.axiom.application.features.attachments.uploadattachment.UploadAttachmentCommandHandler;
import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.*;
import com.studproj.axiom.domain.repository.*;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentHandlersTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WORK_ITEM_ID = UUID.randomUUID();

    private WorkItem workItem;
    private Project project;

    @BeforeEach
    void setUpCommon() {
        workItem = WorkItem.builder()
                .id(WORK_ITEM_ID).controlNo(1).description("T")
                .type(WorkItemType.Task).status(WorkItemStatus.New)
                .projectId(PROJECT_ID).authorId(USER_ID).build();
        project = new Project(PROJECT_ID, "P", "P", "d", LocalDateTime.now(), USER_ID);
    }

    @Nested
    class UploadAttachmentTests {
        @Mock private AttachmentRepository attachmentRepository;
        @Mock private AttachmentObjectRepository attachmentObjectRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private UploadAttachmentCommandHandler handler;

        @Test
        void shouldUploadAttachmentSuccessfully() {
            byte[] content = "file-content".getBytes();
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID))
                    .thenReturn(Optional.of(new ProjectMembership(
                            UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now())));

            UploadAttachmentCommand command = new UploadAttachmentCommand(WORK_ITEM_ID, "test.pdf", "application/pdf", content);
            UUID result = handler.handle(command);

            assertThat(result).isNotNull();
            verify(attachmentObjectRepository).save(anyString(), eq(content), eq("application/pdf"));
            verify(attachmentRepository).save(any(Attachment.class));
        }

        @Test
        void shouldThrowWhenFileIsEmpty() {
            assertThatThrownBy(() -> handler.handle(new UploadAttachmentCommand(WORK_ITEM_ID, "f.txt", "text/plain", new byte[0])))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("File can not be empty");
        }

        @Test
        void shouldThrowWhenFileIsNull() {
            assertThatThrownBy(() -> handler.handle(new UploadAttachmentCommand(WORK_ITEM_ID, "f.txt", "text/plain", null)))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("File can not be empty");
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new UploadAttachmentCommand(WORK_ITEM_ID, "f.txt", "text/plain", "data".getBytes())))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("WorkItem not found");
        }

        @Test
        void shouldDefaultFileTypeWhenBlank() {
            byte[] content = "data".getBytes();
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID))
                    .thenReturn(Optional.of(new ProjectMembership(
                            UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now())));

            handler.handle(new UploadAttachmentCommand(WORK_ITEM_ID, "file", "", content));

            verify(attachmentObjectRepository).save(anyString(), eq(content), eq("application/octet-stream"));
        }
    }

    @Nested
    class DownloadAttachmentTests {
        @Mock private AttachmentRepository attachmentRepository;
        @Mock private AttachmentObjectRepository attachmentObjectRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private DownloadAttachmentQueryHandler handler;

        private UUID attachmentId;
        private Attachment attachment;

        @BeforeEach
        void setUp() {
            attachmentId = UUID.randomUUID();
            attachment = new Attachment(attachmentId, WORK_ITEM_ID, USER_ID, "key/path", "file.pdf", 1024L, "application/pdf", LocalDateTime.now());
        }

        @Test
        void shouldDownloadSuccessfully() {
            byte[] fileBytes = "pdf-content".getBytes();
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID))
                    .thenReturn(Optional.of(new ProjectMembership(
                            UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now())));
            when(attachmentObjectRepository.findByObjectKey("key/path")).thenReturn(fileBytes);

            DownloadAttachmentResult result = handler.handle(new DownloadAttachmentQuery(attachmentId));

            assertThat(result.fileName()).isEqualTo("file.pdf");
            assertThat(result.fileType()).isEqualTo("application/pdf");
            assertThat(result.content()).isEqualTo(fileBytes);
        }

        @Test
        void shouldThrowWhenAttachmentNotFound() {
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DownloadAttachmentQuery(attachmentId)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Attachment not found");
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DownloadAttachmentQuery(attachmentId)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("WorkItem not found");
        }
    }

    @Nested
    class DeleteAttachmentTests {
        @Mock private AttachmentRepository attachmentRepository;
        @Mock private AttachmentObjectRepository attachmentObjectRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private ProjectRoleRepository projectRoleRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private DeleteAttachmentCommandHandler handler;

        private UUID attachmentId;
        private Attachment attachment;

        @BeforeEach
        void setUp() {
            attachmentId = UUID.randomUUID();
            attachment = new Attachment(attachmentId, WORK_ITEM_ID, USER_ID, "key/path", "file.pdf", 1024L, "application/pdf", LocalDateTime.now());
        }

        @Test
        void shouldDeleteWhenUserIsUploader() {
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID))
                    .thenReturn(Optional.of(new ProjectMembership(
                            UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now())));

            handler.handle(new DeleteAttachmentCommand(attachmentId));

            verify(attachmentObjectRepository).delete("key/path");
            verify(attachmentRepository).delete(attachmentId);
        }

        @Test
        void shouldThrowWhenAttachmentNotFound() {
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new DeleteAttachmentCommand(attachmentId)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Attachment not found");
        }

        @Test
        void shouldThrowWhenNotUploaderNorAdmin() {
            UUID otherUserId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();
            Attachment otherAttachment = new Attachment(attachmentId, WORK_ITEM_ID, otherUserId, "key/path", "file.pdf", 1024L, "application/pdf", LocalDateTime.now());

            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(otherAttachment));
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID))
                    .thenReturn(Optional.of(new ProjectMembership(
                            UUID.randomUUID(), PROJECT_ID, USER_ID, roleId, LocalDateTime.now())));
            when(projectMembershipRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).thenReturn(true);
            when(projectRoleRepository.findById(roleId)).thenReturn(Optional.of(new ProjectRole(roleId, ProjectRoleType.MEMBER)));

            assertThatThrownBy(() -> handler.handle(new DeleteAttachmentCommand(attachmentId)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only the uploader or project admin can delete this attachment");
        }
    }

    @Nested
    class GetAttachmentsByWorkItemTests {
        @Mock private AttachmentRepository attachmentRepository;
        @Mock private WorkItemRepository workItemRepository;
        @Mock private ProjectRepository projectRepository;
        @Mock private ProjectMembershipRepository projectMembershipRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQueryHandler handler;

        @Test
        void shouldReturnAttachmentsWhenMember() {
            Attachment a1 = new Attachment(UUID.randomUUID(), WORK_ITEM_ID, USER_ID, "k1", "f1.txt", 100L, "text/plain", LocalDateTime.now());
            Attachment a2 = new Attachment(UUID.randomUUID(), WORK_ITEM_ID, USER_ID, "k2", "f2.pdf", 200L, "application/pdf", LocalDateTime.now());

            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID))
                    .thenReturn(Optional.of(new ProjectMembership(UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now())));
            when(attachmentRepository.findByWorkItemIdOrderByUploadedOnDesc(WORK_ITEM_ID)).thenReturn(java.util.List.of(a1, a2));

            var result = handler.handle(new com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQuery(WORK_ITEM_ID));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).fileName()).isEqualTo("f1.txt");
        }

        @Test
        void shouldThrowWhenWorkItemNotFound() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQuery(WORK_ITEM_ID)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("WorkItem not found");
        }

        @Test
        void shouldReturnEmptyListWhenNoAttachments() {
            when(workItemRepository.findById(WORK_ITEM_ID)).thenReturn(Optional.of(workItem));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(projectMembershipRepository.findByProjectIdAndUserId(PROJECT_ID, USER_ID))
                    .thenReturn(Optional.of(new ProjectMembership(UUID.randomUUID(), PROJECT_ID, USER_ID, UUID.randomUUID(), LocalDateTime.now())));
            when(attachmentRepository.findByWorkItemIdOrderByUploadedOnDesc(WORK_ITEM_ID)).thenReturn(java.util.List.of());

            var result = handler.handle(new com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQuery(WORK_ITEM_ID));

            assertThat(result).isEmpty();
        }
    }
}
