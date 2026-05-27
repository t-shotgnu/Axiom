package com.studproj.axiom.application.features.auth.refreshtoken;

public record RefreshTokenResponse(
    String token,
    String refreshToken
) {}
