package com.studproj.axiom.application.dto.query;

import java.util.UUID;

public record UserDto(
    UUID id,
    String userName,
    String emailAddress,
    boolean active
) {}
