package com.studproj.axiom.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateProjectCommand(
    @NotBlank String name,
    @NotBlank String code,
    String description,
    @NotNull UUID ownerId
) {}
