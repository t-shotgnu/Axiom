package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.infrastructure.persistence.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AttachmentJpaRepository extends JpaRepository<AttachmentEntity, UUID> {
    List<AttachmentEntity> findByWorkItemId(UUID workItemId);
}
