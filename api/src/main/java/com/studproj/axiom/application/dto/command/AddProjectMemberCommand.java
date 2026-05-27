package com.studproj.axiom.application.dto.command;

import com.studproj.axiom.domain.model.ProjectRoleType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberCommand(
        @NotNull UUID userId,
        @NotNull ProjectRoleType role
) {}