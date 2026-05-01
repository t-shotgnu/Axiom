package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.ProjectDto;
import com.studproj.axiom.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryHandler {
    private final ProjectRepository projectRepository;

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(p -> new ProjectDto(p.getId(), p.getName(), p.getCode(), p.getDescription(), p.getCreatedOn(), p.getOwnerId()))
                .collect(Collectors.toList());
    }

    public Optional<ProjectDto> getProjectById(UUID id) {
        return projectRepository.findById(id)
                .map(p -> new ProjectDto(p.getId(), p.getName(), p.getCode(), p.getDescription(), p.getCreatedOn(), p.getOwnerId()));
    }
}

