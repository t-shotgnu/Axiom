package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateProjectCommand;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectCommandHandler {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public UUID createProject(CreateProjectCommand command) {
        String email = authenticatedUserProvider.getAuthenticatedUserEmail();
        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Project project = Project.builder()
                .id(UUID.randomUUID())
                .name(command.name())
                .code(command.code())
                .description(command.description())
                .createdOn(LocalDateTime.now())
                .ownerId(user.getId())
                .build();

        projectRepository.save(project);
        return project.getId();
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.delete(id);
    }
}

