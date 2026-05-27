package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.projectmembers.addprojectmember.AddProjectMemberCommand;
import com.studproj.axiom.application.features.projectmembers.addprojectmember.AddProjectMemberCommandHandler;
import com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole.ChangeProjectMemberRoleCommand;
import com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole.ChangeProjectMemberRoleCommandHandler;
import com.studproj.axiom.application.features.projectmembers.removeprojectmember.RemoveProjectMemberCommand;
import com.studproj.axiom.application.features.projectmembers.removeprojectmember.RemoveProjectMemberCommandHandler;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.GetProjectMembersQuery;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.GetProjectMembersQueryHandler;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.ProjectMemberDto;
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
    private final GetProjectMembersQueryHandler getProjectMembersQueryHandler;
    private final AddProjectMemberCommandHandler addProjectMemberCommandHandler;
    private final ChangeProjectMemberRoleCommandHandler changeProjectMemberRoleCommandHandler;
    private final RemoveProjectMemberCommandHandler removeProjectMemberCommandHandler;

    @GetMapping
    public ResponseEntity<List<ProjectMemberDto>> getProjectMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(getProjectMembersQueryHandler.handle(new GetProjectMembersQuery(projectId)));
    }

    @PostMapping
    public ResponseEntity<Void> addProjectMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberCommand command
    ) {
        addProjectMemberCommandHandler.handle(projectId, command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Void> changeProjectMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeProjectMemberRoleCommand command
    ) {
        changeProjectMemberRoleCommandHandler.handle(projectId, userId, command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeProjectMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        removeProjectMemberCommandHandler.handle(new RemoveProjectMemberCommand(projectId, userId));
        return ResponseEntity.noContent().build();
    }
}
