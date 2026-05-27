package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.infrastructure.persistence.entity.ProjectMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMembershipJpaRepository extends JpaRepository<ProjectMembershipEntity, UUID> {
    Optional<ProjectMembershipEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<ProjectMembershipEntity> findByProjectId(UUID projectId);

    List<ProjectMembershipEntity> findByUserId(UUID userId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);

    void deleteByProjectId(UUID projectId);
}