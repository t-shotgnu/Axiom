package com.studproj.axiom.application.dto.query;

import com.studproj.axiom.domain.model.ProjectRoleType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectMemberDto(
        UUID userId,
        String userName,
        String emailAddress,
        String firstName,
        String lastName,
        ProjectRoleType role,
        LocalDateTime createdOn
) {}