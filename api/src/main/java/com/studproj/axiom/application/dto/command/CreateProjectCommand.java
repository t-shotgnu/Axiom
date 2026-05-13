package com.studproj.axiom.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectCommand(
    @NotBlank String name,
    @NotBlank String code,
    String description
) {}
