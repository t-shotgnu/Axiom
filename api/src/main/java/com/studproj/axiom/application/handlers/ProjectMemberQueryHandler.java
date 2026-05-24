package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.ProjectMemberDto;
import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberQueryHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public List<ProjectMemberDto> getProjectMembers(java.util.UUID projectId) {
        ProjectAccessChecks.ensureProjectMember(projectRepository, projectMembershipRepository, authenticatedUserProvider, projectId);

        return projectMembershipRepository.findByProjectId(projectId).stream()
                .map(membership -> {
                    var user = userRepository.findById(membership.getUserId()).orElseThrow();
                    ProjectRole role = projectRoleRepository.findById(membership.getRoleId())
                            .orElseThrow();
                    return new ProjectMemberDto(
                            user.getId(),
                            user.getUserName(),
                            user.getEmailAddress(),
                            user.getFirstName(),
                            user.getLastName(),
                            role.getType(),
                            membership.getCreatedOn());
                })
                .toList();
    }
}