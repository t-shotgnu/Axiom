package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.ProjectMembershipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaProjectMembershipRepository implements ProjectMembershipRepository {
    private final ProjectMembershipJpaRepository jpaRepository;

    @Override
    public void save(ProjectMembership membership) {
        jpaRepository.save(ProjectMembershipMapper.toEntity(membership));
    }

    @Override
    public Optional<ProjectMembership> findByProjectIdAndUserId(UUID projectId, UUID userId) {
        return jpaRepository.findByProjectIdAndUserId(projectId, userId).map(ProjectMembershipMapper::toDomain);
    }

    @Override
    public List<ProjectMembership> findByProjectId(UUID projectId) {
        return jpaRepository.findByProjectId(projectId).stream().map(ProjectMembershipMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ProjectMembership> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(ProjectMembershipMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByProjectIdAndUserId(UUID projectId, UUID userId) {
        return jpaRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public void deleteByProjectIdAndUserId(UUID projectId, UUID userId) {
        jpaRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public void deleteByProjectId(UUID projectId) {
        jpaRepository.deleteByProjectId(projectId);
    }
}