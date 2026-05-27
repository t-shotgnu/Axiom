package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRoleRepository {
    void save(ProjectRole role);

    void saveAll(List<ProjectRole> roles);

    Optional<ProjectRole> findByType(ProjectRoleType type);

    Optional<ProjectRole> findById(UUID id);

    List<ProjectRole> findAll();
}