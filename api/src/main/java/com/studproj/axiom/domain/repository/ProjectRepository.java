package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.Project;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    void save(Project project);

    Optional<Project> findById(UUID id);

    List<Project> findByIds(Collection<UUID> ids);

    Optional<Project> findByCode(String code);

    List<Project> findAll();

    long countAll();

    void delete(UUID id);
}
