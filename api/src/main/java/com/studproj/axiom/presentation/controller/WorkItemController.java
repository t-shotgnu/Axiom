package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.dto.command.CreateWorkItemCommand;
import com.studproj.axiom.application.dto.command.AssignWorkItemCommand;
import com.studproj.axiom.application.dto.command.UpdateWorkItemStatusCommand;
import com.studproj.axiom.application.dto.command.UpdateWorkItemNotesCommand;
import com.studproj.axiom.application.dto.query.WorkItemDto;
import com.studproj.axiom.application.handlers.WorkItemCommandHandler;
import com.studproj.axiom.application.handlers.WorkItemQueryHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-items")
@RequiredArgsConstructor
public class WorkItemController {
    private final WorkItemCommandHandler commandService;
    private final WorkItemQueryHandler queryService;

    @PostMapping
    public ResponseEntity<UUID> createWorkItem(@Valid @RequestBody CreateWorkItemCommand command) {
        UUID workItemId = commandService.createWorkItem(command);
        return new ResponseEntity<>(workItemId, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<WorkItemDto>> getWorkItemsByProject(@RequestParam UUID projectId) {
        return ResponseEntity.ok(queryService.getWorkItemsByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkItemDto> getWorkItem(@PathVariable UUID id) {
        return queryService.getWorkItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/assignee")
    public ResponseEntity<Void> assignWorkItem(@PathVariable UUID id, @RequestBody AssignWorkItemCommand command) {
        commandService.assignWorkItem(id, command.assigneeId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @RequestBody UpdateWorkItemStatusCommand command) {
        commandService.updateStatus(id, command.status());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<Void> updateNotes(@PathVariable UUID id, @RequestBody UpdateWorkItemNotesCommand command) {
        commandService.updateNotes(id, command.notes());
        return ResponseEntity.ok().build();
    }
}
