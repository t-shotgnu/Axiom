package com.studproj.axiom.application.dto.response;

import java.util.UUID;

public record AuthResponse(
    String token,
    UUID id,
    String userName,
    String emailAddress
) {}
