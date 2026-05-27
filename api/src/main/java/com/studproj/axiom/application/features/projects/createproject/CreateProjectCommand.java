package com.studproj.axiom.application.features.projects.createproject;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectCommand(
    @NotBlank String name,
    @NotBlank String code,
    String description
) {}
