package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.AddProjectMemberCommand;
import com.studproj.axiom.application.dto.command.ChangeProjectMemberRoleCommand;
import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.exception.NotFoundException;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
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
public class ProjectMemberCommandHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void addMember(UUID projectId, AddProjectMemberCommand command) {
        ProjectAccessChecks.ensureProjectAdmin(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider, projectId);

        if (projectMembershipRepository.existsByProjectIdAndUserId(projectId, command.userId())) {
            throw new BadRequestException("User is already a member of this project");
        }

        userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        var role = projectRoleRepository.findByType(command.role())
                .orElseThrow(() -> new NotFoundException("Project role not found"));

        projectMembershipRepository.save(ProjectMembership.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .userId(command.userId())
                .roleId(role.getId())
                .createdOn(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void changeRole(UUID projectId, UUID userId, ChangeProjectMemberRoleCommand command) {
        ProjectAccessChecks.ensureProjectAdmin(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider, projectId);

        ProjectMembership membership = projectMembershipRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Project member not found"));

        var role = projectRoleRepository.findByType(command.role())
                .orElseThrow(() -> new NotFoundException("Project role not found"));

        membership.setRoleId(role.getId());
        projectMembershipRepository.save(membership);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        ProjectAccessChecks.ensureProjectAdmin(projectRepository, projectMembershipRepository, projectRoleRepository, authenticatedUserProvider, projectId);

        if (projectMembershipRepository.findByProjectIdAndUserId(projectId, userId).isEmpty()) {
            throw new NotFoundException("Project member not found");
        }

        projectMembershipRepository.deleteByProjectIdAndUserId(projectId, userId);
    }
}