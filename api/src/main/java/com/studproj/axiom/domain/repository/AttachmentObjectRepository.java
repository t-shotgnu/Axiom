package com.studproj.axiom.domain.repository;

public interface AttachmentObjectRepository {
    void save(String objectKey, byte[] content, String contentType);

    byte[] findByObjectKey(String objectKey);

    void delete(String objectKey);
}
