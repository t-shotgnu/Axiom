package com.studproj.axiom.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UpdateUserProfileCommand(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull @Past LocalDate dateOfBirth
) {}
