package com.studproj.axiom.infrastructure.storage;

import com.studproj.axiom.domain.repository.AttachmentObjectRepository;
import com.studproj.axiom.infrastructure.config.S3StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Repository
@RequiredArgsConstructor
public class S3AttachmentObjectRepository implements AttachmentObjectRepository {
    private final S3Client s3Client;
    private final S3StorageProperties properties;

    @Override
    public void save(String objectKey, byte[] content, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));
    }

    @Override
    public byte[] findByObjectKey(String objectKey) {
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(builder -> builder
                .bucket(properties.bucket())
                .key(objectKey));
        return response.asByteArray();
    }

    @Override
    public void delete(String objectKey) {
        s3Client.deleteObject(builder -> builder
                .bucket(properties.bucket())
                .key(objectKey));
    }
}
