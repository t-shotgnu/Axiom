package com.studproj.axiom.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenCommand(
    @NotBlank String refreshToken
) {}
