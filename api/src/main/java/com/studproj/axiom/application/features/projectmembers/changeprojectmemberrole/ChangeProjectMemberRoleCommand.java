package com.studproj.axiom.application.features.projectmembers.changeprojectmemberrole;

import com.studproj.axiom.domain.model.ProjectRoleType;
import jakarta.validation.constraints.NotNull;

public record ChangeProjectMemberRoleCommand(
    @NotNull ProjectRoleType role
) {}
