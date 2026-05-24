package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.ProjectMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMembershipRepository {
    void save(ProjectMembership membership);

    Optional<ProjectMembership> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<ProjectMembership> findByProjectId(UUID projectId);

    List<ProjectMembership> findByUserId(UUID userId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);

    void deleteByProjectId(UUID projectId);
}