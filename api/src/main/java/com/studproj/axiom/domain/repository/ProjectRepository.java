package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    void save(Project project);

    Optional<Project> findById(UUID id);

    List<Project> findAll();

    void delete(UUID id);
}
