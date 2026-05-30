package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.workitems.createworkitem.CreateWorkItemCommand;
import com.studproj.axiom.application.features.workitems.createworkitem.CreateWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.assignworkitem.AssignWorkItemCommand;
import com.studproj.axiom.application.features.workitems.assignworkitem.AssignWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.updateworkitem.UpdateWorkItemCommand;
import com.studproj.axiom.application.features.workitems.updateworkitem.UpdateWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.updateworkitemstatus.UpdateWorkItemStatusCommand;
import com.studproj.axiom.application.features.workitems.updateworkitemstatus.UpdateWorkItemStatusCommandHandler;
import com.studproj.axiom.application.features.workitems.updateworkitemnotes.UpdateWorkItemNotesCommand;
import com.studproj.axiom.application.features.workitems.updateworkitemnotes.UpdateWorkItemNotesCommandHandler;
import com.studproj.axiom.application.features.workitems.deleteworkitem.DeleteWorkItemCommand;
import com.studproj.axiom.application.features.workitems.deleteworkitem.DeleteWorkItemCommandHandler;
import com.studproj.axiom.application.features.workitems.getworkitembyid.GetWorkItemByIdQuery;
import com.studproj.axiom.application.features.workitems.getworkitembyid.GetWorkItemByIdQueryHandler;
import com.studproj.axiom.application.features.workitems.getworkitemsbyproject.GetWorkItemsByProjectQuery;
import com.studproj.axiom.application.features.workitems.getworkitemsbyproject.GetWorkItemsByProjectQueryHandler;
import com.studproj.axiom.application.features.workitems.WorkItemDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final CreateWorkItemCommandHandler createWorkItemCommandHandler;
    private final AssignWorkItemCommandHandler assignWorkItemCommandHandler;
    private final UpdateWorkItemCommandHandler updateWorkItemCommandHandler;
    private final UpdateWorkItemStatusCommandHandler updateWorkItemStatusCommandHandler;
    private final UpdateWorkItemNotesCommandHandler updateWorkItemNotesCommandHandler;
    private final DeleteWorkItemCommandHandler deleteWorkItemCommandHandler;
    private final GetWorkItemByIdQueryHandler getWorkItemByIdQueryHandler;
    private final GetWorkItemsByProjectQueryHandler getWorkItemsByProjectQueryHandler;

    @PostMapping
    public ResponseEntity<UUID> createWorkItem(@Valid @RequestBody CreateWorkItemCommand command) {
        UUID workItemId = createWorkItemCommandHandler.handle(command);
        return new ResponseEntity<>(workItemId, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<WorkItemDto>> getWorkItemsByProject(@RequestParam UUID projectId) {
        return ResponseEntity.ok(getWorkItemsByProjectQueryHandler.handle(new GetWorkItemsByProjectQuery(projectId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkItemDto> getWorkItem(@PathVariable UUID id) {
        return getWorkItemByIdQueryHandler.handle(new GetWorkItemByIdQuery(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/assignee")
    public ResponseEntity<Void> assignWorkItem(@PathVariable UUID id, @Valid @RequestBody com.studproj.axiom.presentation.controller.dto.AssignWorkItemRequest request) {
        assignWorkItemCommandHandler.handle(new AssignWorkItemCommand(id, request.assigneeId()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody com.studproj.axiom.presentation.controller.dto.UpdateWorkItemStatusRequest request) {
        updateWorkItemStatusCommandHandler.handle(new UpdateWorkItemStatusCommand(id, request.status()));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateWorkItem(@PathVariable UUID id, @Valid @RequestBody com.studproj.axiom.presentation.controller.dto.UpdateWorkItemRequest request) {
        updateWorkItemCommandHandler.handle(new UpdateWorkItemCommand(
                id,
                request.description(),
                request.priority(),
                request.type(),
                request.status(),
                request.dueDate(),
                request.estimatedEffort(),
                request.assigneeId(),
                request.notes()
        ));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<Void> updateNotes(@PathVariable UUID id, @Valid @RequestBody com.studproj.axiom.presentation.controller.dto.UpdateWorkItemNotesRequest request) {
        updateWorkItemNotesCommandHandler.handle(new UpdateWorkItemNotesCommand(id, request.notes()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkItem(@PathVariable UUID id) {
        deleteWorkItemCommandHandler.handle(new DeleteWorkItemCommand(id));
        return ResponseEntity.noContent().build();
    }
}
