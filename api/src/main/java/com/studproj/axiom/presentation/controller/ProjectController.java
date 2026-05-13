package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.dto.command.CreateProjectCommand;
import com.studproj.axiom.application.dto.query.ProjectDto;
import com.studproj.axiom.application.handlers.ProjectCommandHandler;
import com.studproj.axiom.infrastructure.query.ProjectQueryHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectCommandHandler commandService;
    private final ProjectQueryHandler queryService;

    @PostMapping
    public ResponseEntity<UUID> createProject(@Valid @RequestBody CreateProjectCommand command) {
        UUID projectId = commandService.createProject(command);
        return new ResponseEntity<>(projectId, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        return ResponseEntity.ok(queryService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable UUID id) {
        return queryService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        commandService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
