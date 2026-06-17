package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.projectmembers.addprojectmember.AddProjectMemberCommand;
import com.studproj.axiom.application.features.projectmembers.addprojectmember.AddProjectMemberCommandHandler;
import com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole.ChangeProjectMemberRoleCommand;
import com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole.ChangeProjectMemberRoleCommandHandler;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.GetProjectMembersQuery;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.GetProjectMembersQueryHandler;
import com.studproj.axiom.application.features.projectmembers.getprojectmembers.ProjectMemberDto;
import com.studproj.axiom.application.features.projectmembers.removeprojectmember.RemoveProjectMemberCommand;
import com.studproj.axiom.application.features.projectmembers.removeprojectmember.RemoveProjectMemberCommandHandler;
import com.studproj.axiom.domain.model.ProjectRoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectMemberControllerTest {

    private GetProjectMembersQueryHandler getHandler;
    private AddProjectMemberCommandHandler addHandler;
    private ChangeProjectMemberRoleCommandHandler changeRoleHandler;
    private RemoveProjectMemberCommandHandler removeHandler;
    private ProjectMemberController controller;

    @BeforeEach
    void setUp() {
        getHandler = mock(GetProjectMembersQueryHandler.class);
        addHandler = mock(AddProjectMemberCommandHandler.class);
        changeRoleHandler = mock(ChangeProjectMemberRoleCommandHandler.class);
        removeHandler = mock(RemoveProjectMemberCommandHandler.class);
        controller = new ProjectMemberController(getHandler, addHandler, changeRoleHandler, removeHandler);
    }

    @Test
    void getProjectMembersDelegatesQuery() {
        UUID projectId = UUID.randomUUID();
        List<ProjectMemberDto> members = List.of(new ProjectMemberDto(
                UUID.randomUUID(),
                "ada",
                "ada@example.com",
                "Ada",
                "Lovelace",
                ProjectRoleType.ADMIN,
                LocalDateTime.of(2026, 1, 1, 12, 0)
        ));
        when(getHandler.handle(new GetProjectMembersQuery(projectId))).thenReturn(members);

        var response = controller.getProjectMembers(projectId);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(members);
    }

    @Test
    void addProjectMemberDelegatesCommandAndReturnsCreated() {
        UUID projectId = UUID.randomUUID();
        AddProjectMemberCommand command = new AddProjectMemberCommand(UUID.randomUUID(), ProjectRoleType.MEMBER);

        var response = controller.addProjectMember(projectId, command);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        verify(addHandler).handle(projectId, command);
    }

    @Test
    void changeProjectMemberRoleDelegatesCommandAndReturnsNoContent() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ChangeProjectMemberRoleCommand command = new ChangeProjectMemberRoleCommand(ProjectRoleType.ADMIN);

        var response = controller.changeProjectMemberRole(projectId, userId, command);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(changeRoleHandler).handle(projectId, userId, command);
    }

    @Test
    void removeProjectMemberDelegatesCommandAndReturnsNoContent() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var response = controller.removeProjectMember(projectId, userId);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(removeHandler).handle(new RemoveProjectMemberCommand(projectId, userId));
    }
}
