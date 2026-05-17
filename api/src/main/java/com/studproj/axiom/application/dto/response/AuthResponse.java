package com.studproj.axiom.application.dto.response;

public record AuthResponse(
    String token,
    String refreshToken
) {}
