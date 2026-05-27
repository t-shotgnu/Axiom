package com.studproj.axiom.application.features.auth.registeruser;

public record RegisterUserResponse(
    String token,
    String refreshToken
) {}
