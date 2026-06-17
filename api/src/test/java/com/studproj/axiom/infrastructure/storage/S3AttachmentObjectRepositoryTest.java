package com.studproj.axiom.infrastructure.storage;

import com.studproj.axiom.infrastructure.config.S3StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3AttachmentObjectRepositoryTest {

    private S3Client s3Client;
    private S3AttachmentObjectRepository repository;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        S3StorageProperties properties = new S3StorageProperties(
                "http://localhost:9000",
                "us-east-1",
                "axiom-test",
                "access",
                "secret"
        );
        repository = new S3AttachmentObjectRepository(s3Client, properties);
    }

    @Test
    void saveBuildsPutObjectRequestWithBucketKeyTypeAndLength() throws Exception {
        byte[] content = "hello".getBytes();

        repository.save("attachments/hello.txt", content, "text/plain");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("axiom-test");
        assertThat(request.key()).isEqualTo("attachments/hello.txt");
        assertThat(request.contentType()).isEqualTo("text/plain");
        assertThat(request.contentLength()).isEqualTo(5L);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bodyCaptor.getValue().contentStreamProvider().newStream().transferTo(output);
        assertThat(output.toByteArray()).isEqualTo(content);
        assertThat(bodyCaptor.getValue().optionalContentLength()).isEqualTo(Optional.of(5L));
    }

    @Test
    void findByObjectKeyBuildsGetRequestAndReturnsBytes() {
        byte[] content = "payload".getBytes();
        when(s3Client.getObjectAsBytes(org.mockito.ArgumentMatchers.<Consumer<GetObjectRequest.Builder>>any()))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content));

        byte[] result = repository.findByObjectKey("attachments/payload.bin");

        ArgumentCaptor<Consumer<GetObjectRequest.Builder>> requestCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(s3Client).getObjectAsBytes(requestCaptor.capture());

        GetObjectRequest.Builder builder = GetObjectRequest.builder();
        requestCaptor.getValue().accept(builder);
        GetObjectRequest request = builder.build();

        assertThat(request.bucket()).isEqualTo("axiom-test");
        assertThat(request.key()).isEqualTo("attachments/payload.bin");
        assertThat(result).isEqualTo(content);
    }

    @Test
    void deleteBuildsDeleteRequest() {
        repository.delete("attachments/remove.txt");

        ArgumentCaptor<Consumer<DeleteObjectRequest.Builder>> requestCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(s3Client).deleteObject(requestCaptor.capture());

        DeleteObjectRequest.Builder builder = DeleteObjectRequest.builder();
        requestCaptor.getValue().accept(builder);
        DeleteObjectRequest request = builder.build();

        assertThat(request.bucket()).isEqualTo("axiom-test");
        assertThat(request.key()).isEqualTo("attachments/remove.txt");
    }
}
