package com.studproj.axiom.application.handlers;

import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.repository.ProjectRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectQueryHandlerTest {

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectQueryHandler handler = new ProjectQueryHandler(projectRepository);

    @Test
    void getAllProjectsMapsDomainModelsToDtos() {
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        LocalDateTime createdOn = LocalDateTime.now();
        when(projectRepository.findAll()).thenReturn(List.of(Project.builder()
                .id(projectId)
                .name("Axiom")
                .code("AX")
                .description("Main project")
                .createdOn(createdOn)
                .ownerId(ownerId)
                .build()));

        var projects = handler.getAllProjects();

        assertThat(projects).hasSize(1);
        assertThat(projects.getFirst().id()).isEqualTo(projectId);
        assertThat(projects.getFirst().name()).isEqualTo("Axiom");
        assertThat(projects.getFirst().code()).isEqualTo("AX");
        assertThat(projects.getFirst().description()).isEqualTo("Main project");
        assertThat(projects.getFirst().createdOn()).isEqualTo(createdOn);
        assertThat(projects.getFirst().ownerId()).isEqualTo(ownerId);
    }

    @Test
    void getProjectByIdReturnsMappedDtoWhenFound() {
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(Project.builder()
                .id(projectId)
                .name("Axiom")
                .code("AX")
                .description("")
                .createdOn(LocalDateTime.now())
                .ownerId(ownerId)
                .build()));

        var project = handler.getProjectById(projectId);

        assertThat(project).isPresent();
        assertThat(project.orElseThrow().id()).isEqualTo(projectId);
        assertThat(project.orElseThrow().ownerId()).isEqualTo(ownerId);
    }

    @Test
    void getProjectByIdReturnsEmptyWhenMissing() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThat(handler.getProjectById(projectId)).isEmpty();
    }
}
