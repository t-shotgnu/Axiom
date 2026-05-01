package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateProjectCommand;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectCommandHandler {
    private final ProjectRepository projectRepository;

    @Transactional
    public UUID createProject(CreateProjectCommand command) {
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .name(command.name())
                .code(command.code())
                .description(command.description())
                .createdOn(LocalDateTime.now())
                .ownerId(command.ownerId())
                .build();

        projectRepository.save(project);
        return project.getId();
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.delete(id);
    }
}

