package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.taskrelationships.TaskRelationshipDto;
import com.studproj.axiom.application.features.taskrelationships.createtaskrelationship.CreateTaskRelationshipCommand;
import com.studproj.axiom.application.features.taskrelationships.createtaskrelationship.CreateTaskRelationshipCommandHandler;
import com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship.DeleteTaskRelationshipCommand;
import com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship.DeleteTaskRelationshipCommandHandler;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByProjectQuery;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByProjectQueryHandler;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByWorkItemQuery;
import com.studproj.axiom.application.features.taskrelationships.gettaskrelationships.GetTaskRelationshipsByWorkItemQueryHandler;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-relationships")
@RequiredArgsConstructor
public class TaskRelationshipController {
    private final CreateTaskRelationshipCommandHandler createHandler;
    private final DeleteTaskRelationshipCommandHandler deleteHandler;
    private final GetTaskRelationshipsByProjectQueryHandler getByProjectHandler;
    private final GetTaskRelationshipsByWorkItemQueryHandler getByWorkItemHandler;

    @PostMapping
    public ResponseEntity<UUID> createRelationship(@Valid @RequestBody CreateTaskRelationshipCommand command) {
        UUID relationshipId = createHandler.handle(command);
        return new ResponseEntity<>(relationshipId, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRelationship(@PathVariable UUID id) {
        deleteHandler.handle(new DeleteTaskRelationshipCommand(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskRelationshipDto>> getRelationshipsByProject(@RequestParam UUID projectId) {
        return ResponseEntity.ok(getByProjectHandler.handle(new GetTaskRelationshipsByProjectQuery(projectId)));
    }

    @GetMapping("/work-item/{id}")
    public ResponseEntity<List<TaskRelationshipDto>> getRelationshipsByWorkItem(@PathVariable UUID id) {
        return ResponseEntity.ok(getByWorkItemHandler.handle(new GetTaskRelationshipsByWorkItemQuery(id)));
    }
}
