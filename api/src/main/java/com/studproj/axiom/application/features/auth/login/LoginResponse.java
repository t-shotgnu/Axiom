package com.studproj.axiom.application.features.auth.login;

public record LoginResponse(
    String token,
    String refreshToken
) {}
