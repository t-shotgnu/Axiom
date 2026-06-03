package com.studproj.axiom.application.features.projectmembers.addprojectmember;

import com.studproj.axiom.application.features.projects.ProjectAccessChecks;
import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.ProjectMembership;
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
public class AddProjectMemberCommandHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(UUID projectId, AddProjectMemberCommand command) {
        ProjectAccessChecks.ensureProjectAdmin(
                projectRepository,
                projectMembershipRepository,
                projectRoleRepository,
                authenticatedUserProvider,
                projectId);

        if (projectMembershipRepository.existsByProjectIdAndUserId(projectId, command.userId())) {
            throw new BadRequestException("User is already a member of this project");
        }

        userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        var role = projectRoleRepository.findByType(command.role())
                .orElseThrow(() -> new NotFoundException("Project role not found"));

        projectMembershipRepository.save(new ProjectMembership(
                UUID.randomUUID(),
                projectId,
                command.userId(),
                role.getId(),
                LocalDateTime.now()
        ));
    }
}
