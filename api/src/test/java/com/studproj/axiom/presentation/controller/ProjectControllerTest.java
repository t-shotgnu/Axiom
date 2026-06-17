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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectControllerTest {

    private CreateProjectCommandHandler createHandler;
    private DeleteProjectCommandHandler deleteHandler;
    private GetAllProjectsQueryHandler getAllHandler;
    private GetProjectDetailsQueryHandler getDetailsHandler;
    private UpdateProjectCommandHandler updateHandler;
    private ProjectController controller;

    @BeforeEach
    void setUp() {
        createHandler = mock(CreateProjectCommandHandler.class);
        deleteHandler = mock(DeleteProjectCommandHandler.class);
        getAllHandler = mock(GetAllProjectsQueryHandler.class);
        getDetailsHandler = mock(GetProjectDetailsQueryHandler.class);
        updateHandler = mock(UpdateProjectCommandHandler.class);
        controller = new ProjectController(createHandler, deleteHandler, getAllHandler, getDetailsHandler, updateHandler);
    }

    @Test
    void createProjectReturnsCreatedId() {
        CreateProjectCommand command = new CreateProjectCommand("Axiom", "AX", "Planning");
        UUID projectId = UUID.randomUUID();
        when(createHandler.handle(command)).thenReturn(projectId);

        var response = controller.createProject(command);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(projectId);
    }

    @Test
    void getAllProjectsDelegatesQuery() {
        List<com.studproj.axiom.application.features.projects.getallprojects.ProjectDto> projects = List.of(
                new com.studproj.axiom.application.features.projects.getallprojects.ProjectDto(
                        UUID.randomUUID(),
                        "Axiom",
                        "AX",
                        "Planning",
                        LocalDateTime.of(2026, 1, 1, 12, 0),
                        UUID.randomUUID(),
                        "Ada"
                )
        );
        when(getAllHandler.handle(new GetAllProjectsQuery())).thenReturn(projects);

        var response = controller.getAllProjects();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(projects);
    }

    @Test
    void getProjectReturnsOkOrNotFound() {
        UUID projectId = UUID.randomUUID();
        var project = new com.studproj.axiom.application.features.projects.getprojectdetails.ProjectDto(
                projectId,
                "Axiom",
                "AX",
                "Planning",
                LocalDateTime.of(2026, 1, 1, 12, 0),
                UUID.randomUUID(),
                "Ada"
        );
        when(getDetailsHandler.handle(new GetProjectDetailsQuery(projectId))).thenReturn(Optional.of(project));

        var found = controller.getProject(projectId);
        assertThat(found.getStatusCode().value()).isEqualTo(200);
        assertThat(found.getBody()).isEqualTo(project);

        UUID missingProjectId = UUID.randomUUID();
        when(getDetailsHandler.handle(new GetProjectDetailsQuery(missingProjectId))).thenReturn(Optional.empty());

        var missing = controller.getProject(missingProjectId);
        assertThat(missing.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void updateProjectDelegatesCommandAndMapsFailuresToBadRequest() throws Exception {
        UUID projectId = UUID.randomUUID();
        UpdateProjectCommand command = new UpdateProjectCommand("Renamed", "RN");

        var response = controller.updateProject(projectId, command);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(updateHandler).handle(projectId, command);

        UUID failingProjectId = UUID.randomUUID();
        doThrow(new RuntimeException("invalid")).when(updateHandler).handle(failingProjectId, command);

        ResponseEntity<Void> failingResponse;
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(OutputStream.nullOutputStream()));
            failingResponse = controller.updateProject(failingProjectId, command);
        } finally {
            System.setErr(originalErr);
        }

        assertThat(failingResponse.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void deleteProjectDelegatesCommand() {
        UUID projectId = UUID.randomUUID();

        var response = controller.deleteProject(projectId);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(deleteHandler).handle(new DeleteProjectCommand(projectId));
    }
}
