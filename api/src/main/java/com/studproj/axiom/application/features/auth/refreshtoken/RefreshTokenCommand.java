package com.studproj.axiom.application.features.auth.refreshtoken;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenCommand(
    @NotBlank String refreshToken
) {}
