package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.Attachment;
import com.studproj.axiom.domain.repository.AttachmentRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaAttachmentRepository implements AttachmentRepository {
    private final AttachmentJpaRepository jpaRepository;

    @Override
    public void save(Attachment attachment) {
        jpaRepository.save(AttachmentMapper.toEntity(attachment));
    }

    @Override
    public Optional<Attachment> findById(UUID id) {
        return jpaRepository.findById(id).map(AttachmentMapper::toDomain);
    }

    @Override
    public List<Attachment> findByWorkItemIdOrderByUploadedOnDesc(UUID workItemId) {
        return jpaRepository.findByWorkItemIdOrderByUploadedOnDesc(workItemId).stream()
                .map(AttachmentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
