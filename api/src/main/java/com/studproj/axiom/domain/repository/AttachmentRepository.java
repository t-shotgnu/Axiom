package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.Attachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository {
    void save(Attachment attachment);

    Optional<Attachment> findById(UUID id);

    List<Attachment> findByWorkItemIdOrderByUploadedOnDesc(UUID workItemId);

    void delete(UUID id);
}
