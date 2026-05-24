package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.dto.command.AddProjectMemberCommand;
import com.studproj.axiom.application.dto.command.ChangeProjectMemberRoleCommand;
import com.studproj.axiom.application.dto.query.ProjectMemberDto;
import com.studproj.axiom.application.handlers.ProjectMemberCommandHandler;
import com.studproj.axiom.application.handlers.ProjectMemberQueryHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {
    private final ProjectMemberQueryHandler queryHandler;
    private final ProjectMemberCommandHandler commandHandler;

    @GetMapping
    public ResponseEntity<List<ProjectMemberDto>> getProjectMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(queryHandler.getProjectMembers(projectId));
    }

    @PostMapping
    public ResponseEntity<Void> addProjectMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberCommand command
    ) {
        commandHandler.addMember(projectId, command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Void> changeProjectMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeProjectMemberRoleCommand command
    ) {
        commandHandler.changeRole(projectId, userId, command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeProjectMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        commandHandler.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}