package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.attachments.AttachmentDto;
import com.studproj.axiom.application.features.attachments.deleteattachment.DeleteAttachmentCommand;
import com.studproj.axiom.application.features.attachments.deleteattachment.DeleteAttachmentCommandHandler;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentQuery;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentQueryHandler;
import com.studproj.axiom.application.features.attachments.downloadattachment.DownloadAttachmentResult;
import com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQuery;
import com.studproj.axiom.application.features.attachments.getattachmentsbyworkitem.GetAttachmentsByWorkItemQueryHandler;
import com.studproj.axiom.application.features.attachments.uploadattachment.UploadAttachmentCommand;
import com.studproj.axiom.application.features.attachments.uploadattachment.UploadAttachmentCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentControllerTest {

    private GetAttachmentsByWorkItemQueryHandler getHandler;
    private UploadAttachmentCommandHandler uploadHandler;
    private DownloadAttachmentQueryHandler downloadHandler;
    private DeleteAttachmentCommandHandler deleteHandler;
    private AttachmentController controller;

    @BeforeEach
    void setUp() {
        getHandler = mock(GetAttachmentsByWorkItemQueryHandler.class);
        uploadHandler = mock(UploadAttachmentCommandHandler.class);
        downloadHandler = mock(DownloadAttachmentQueryHandler.class);
        deleteHandler = mock(DeleteAttachmentCommandHandler.class);
        controller = new AttachmentController(getHandler, uploadHandler, downloadHandler, deleteHandler);
    }

    @Test
    void getAttachmentsDelegatesQuery() {
        UUID workItemId = UUID.randomUUID();
        List<AttachmentDto> attachments = List.of(new AttachmentDto(
                UUID.randomUUID(),
                workItemId,
                UUID.randomUUID(),
                "spec.pdf",
                42L,
                "application/pdf",
                LocalDateTime.of(2026, 1, 1, 12, 0)
        ));
        when(getHandler.handle(new GetAttachmentsByWorkItemQuery(workItemId))).thenReturn(attachments);

        var response = controller.getAttachments(workItemId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(attachments);
    }

    @Test
    void uploadAttachmentReadsMultipartFileAndReturnsCreatedId() throws IOException {
        UUID workItemId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        byte[] bytes = "payload".getBytes();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("payload.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getBytes()).thenReturn(bytes);
        when(uploadHandler.handle(org.mockito.ArgumentMatchers.any(UploadAttachmentCommand.class))).thenReturn(attachmentId);

        var response = controller.uploadAttachment(workItemId, file);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(attachmentId);

        ArgumentCaptor<UploadAttachmentCommand> captor = ArgumentCaptor.forClass(UploadAttachmentCommand.class);
        verify(uploadHandler).handle(captor.capture());
        assertThat(captor.getValue().workItemId()).isEqualTo(workItemId);
        assertThat(captor.getValue().fileName()).isEqualTo("payload.txt");
        assertThat(captor.getValue().fileType()).isEqualTo("text/plain");
        assertThat(captor.getValue().content()).isSameAs(bytes);
    }

    @Test
    void downloadAttachmentBuildsDownloadHeadersAndBody() {
        UUID attachmentId = UUID.randomUUID();
        byte[] content = "report".getBytes();
        when(downloadHandler.handle(new DownloadAttachmentQuery(attachmentId)))
                .thenReturn(new DownloadAttachmentResult("report.csv", "text/csv", content));

        var response = controller.downloadAttachment(attachmentId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment")
                .contains("report.csv");
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/csv");
        assertThat(response.getBody()).isEqualTo(content);
    }

    @Test
    void deleteAttachmentDelegatesCommand() {
        UUID attachmentId = UUID.randomUUID();

        var response = controller.deleteAttachment(attachmentId);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(deleteHandler).handle(new DeleteAttachmentCommand(attachmentId));
    }
}
