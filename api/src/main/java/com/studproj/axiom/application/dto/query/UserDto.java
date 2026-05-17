package com.studproj.axiom.application.dto.query;

import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
    UUID id,
    String userName,
    String emailAddress,
    String firstName,
    String lastName,
    LocalDate dateOfBirth
) {}
