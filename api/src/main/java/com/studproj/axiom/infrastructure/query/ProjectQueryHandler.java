package com.studproj.axiom.infrastructure.query;

import com.studproj.axiom.application.dto.query.ProjectDto;
import com.studproj.axiom.infrastructure.persistence.repository.ProjectJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryHandler {
    private final ProjectJpaRepository projectJpaRepository;

    public List<ProjectDto> getAllProjects() {
        return projectJpaRepository.findAll().stream()
                .map(project -> new ProjectDto(
                        project.getId(),
                        project.getName(),
                        project.getCode(),
                        project.getDescription(),
                        project.getCreatedOn(),
                        project.getOwnerId()))
                .toList();
    }

    public Optional<ProjectDto> getProjectById(UUID id) {
        return projectJpaRepository.findById(id)
                .map(project -> new ProjectDto(
                        project.getId(),
                        project.getName(),
                        project.getCode(),
                        project.getDescription(),
                        project.getCreatedOn(),
                        project.getOwnerId()));
    }
}
