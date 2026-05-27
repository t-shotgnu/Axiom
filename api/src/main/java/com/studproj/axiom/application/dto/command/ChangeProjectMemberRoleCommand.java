package com.studproj.axiom.application.dto.command;

import com.studproj.axiom.domain.model.ProjectRoleType;
import jakarta.validation.constraints.NotNull;

public record ChangeProjectMemberRoleCommand(
        @NotNull ProjectRoleType role
) {}