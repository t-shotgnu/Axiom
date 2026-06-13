package com.studproj.axiom.application.features.projects.updateproject;

import jakarta.validation.constraints.NotBlank;

public record UpdateProjectCommand(
    @NotBlank String name,
    @NotBlank String code
) {}
