package com.studproj.axiom.application.features.projects.createproject;

import com.studproj.axiom.domain.exception.EntityNotFoundException;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateProjectCommandHandler {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public UUID handle(CreateProjectCommand command) {
        String email = authenticatedUserProvider.getAuthenticatedUserEmail();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project(
                UUID.randomUUID(),
                command.name(),
                command.code(),
                command.description(),
                LocalDateTime.now(),
                user.getId()
        );

        projectRepository.save(project);

        var adminRole = projectRoleRepository.findByType(ProjectRoleType.ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("Project role ADMIN not found"));

        projectMembershipRepository.save(new ProjectMembership(
                UUID.randomUUID(),
                project.getId(),
                user.getId(),
                adminRole.getId(),
                LocalDateTime.now()
        ));

        return project.getId();
    }
}
