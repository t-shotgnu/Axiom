package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.LinkType;
import com.studproj.axiom.domain.model.TaskRelationship;
import com.studproj.axiom.domain.repository.TaskRelationshipRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.TaskRelationshipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaTaskRelationshipRepository implements TaskRelationshipRepository {
    private final TaskRelationshipJpaRepository jpaRepository;

    @Override
    public void save(TaskRelationship relationship) {
        jpaRepository.save(TaskRelationshipMapper.toEntity(relationship));
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<TaskRelationship> findById(UUID id) {
        return jpaRepository.findById(id).map(TaskRelationshipMapper::toDomain);
    }

    @Override
    public List<TaskRelationship> findByProjectId(UUID projectId) {
        return jpaRepository.findByProjectId(projectId).stream()
                .map(TaskRelationshipMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskRelationship> findBySourceId(UUID sourceId) {
        return jpaRepository.findBySourceId(sourceId).stream()
                .map(TaskRelationshipMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskRelationship> findByTargetId(UUID targetId) {
        return jpaRepository.findByTargetId(targetId).stream()
                .map(TaskRelationshipMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskRelationship> findBySourceIdAndLinkType(UUID sourceId, LinkType linkType) {
        return jpaRepository.findBySourceIdAndLinkType(sourceId, linkType).stream()
                .map(TaskRelationshipMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskRelationship> findByTargetIdAndLinkType(UUID targetId, LinkType linkType) {
        return jpaRepository.findByTargetIdAndLinkType(targetId, linkType).stream()
                .map(TaskRelationshipMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TaskRelationship> findBySourceIdAndTargetIdAndLinkType(UUID sourceId, UUID targetId, LinkType linkType) {
        return jpaRepository.findBySourceIdAndTargetIdAndLinkType(sourceId, targetId, linkType)
                .map(TaskRelationshipMapper::toDomain);
    }
}
