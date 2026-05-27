package com.studproj.axiom.application.features.projects.getprojectdetails;

import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProjectDetailsQueryHandler {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public Optional<ProjectDto> handle(GetProjectDetailsQuery query) {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        boolean isMember = projectMembershipRepository.existsByProjectIdAndUserId(query.id(), userId);

        return isMember ? projectRepository.findById(query.id())
                .map(this::toDto) : Optional.empty();
    }

    private ProjectDto toDto(com.studproj.axiom.domain.model.Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getCode(),
                project.getDescription(),
                project.getCreatedOn(),
                project.getOwnerId(),
                resolveOwnerName(project.getOwnerId()));
    }

    private String resolveOwnerName(UUID ownerId) {
        return userRepository.findById(ownerId)
                .map(user -> {
                    String fullName = buildFullName(user.getFirstName(), user.getLastName());
                    if (!fullName.isBlank()) {
                        return fullName;
                    }
                    if (user.getUserName() != null && !user.getUserName().isBlank()) {
                        return user.getUserName();
                    }
                    if (user.getEmailAddress() != null && !user.getEmailAddress().isBlank()) {
                        return user.getEmailAddress();
                    }
                    return ownerId.toString();
                })
                .orElse(ownerId.toString());
    }

    private String buildFullName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String fullName = (first + " " + last).trim();
        return fullName.isBlank() ? "" : fullName;
    }
}
