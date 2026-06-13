package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.projects.createproject.CreateProjectCommand;
import com.studproj.axiom.application.features.projects.createproject.CreateProjectCommandHandler;
import com.studproj.axiom.application.features.projects.deleteproject.DeleteProjectCommand;
import com.studproj.axiom.application.features.projects.deleteproject.DeleteProjectCommandHandler;
import com.studproj.axiom.application.features.projects.getallprojects.GetAllProjectsQuery;
import com.studproj.axiom.application.features.projects.getallprojects.GetAllProjectsQueryHandler;
import com.studproj.axiom.application.features.projects.getprojectdetails.GetProjectDetailsQuery;
import com.studproj.axiom.application.features.projects.getprojectdetails.GetProjectDetailsQueryHandler;
import com.studproj.axiom.application.features.projects.updateproject.UpdateProjectCommand;
import com.studproj.axiom.application.features.projects.updateproject.UpdateProjectCommandHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final CreateProjectCommandHandler createProjectCommandHandler;
    private final DeleteProjectCommandHandler deleteProjectCommandHandler;
    private final GetAllProjectsQueryHandler getAllProjectsQueryHandler;
    private final GetProjectDetailsQueryHandler getProjectDetailsQueryHandler;
    private final UpdateProjectCommandHandler updateProjectCommandHandler;

    @PostMapping
    public ResponseEntity<UUID> createProject(@Valid @RequestBody CreateProjectCommand command) {
        UUID projectId = createProjectCommandHandler.handle(command);
        return new ResponseEntity<>(projectId, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<com.studproj.axiom.application.features.projects.getallprojects.ProjectDto>> getAllProjects() {
        return ResponseEntity.ok(getAllProjectsQueryHandler.handle(new GetAllProjectsQuery()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.studproj.axiom.application.features.projects.getprojectdetails.ProjectDto> getProject(@PathVariable UUID id) {
        return getProjectDetailsQueryHandler.handle(new GetProjectDetailsQuery(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectCommand command
    ) {
        try {
            updateProjectCommandHandler.handle(id, command);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        deleteProjectCommandHandler.handle(new DeleteProjectCommand(id));
        return ResponseEntity.noContent().build();
    }
}
