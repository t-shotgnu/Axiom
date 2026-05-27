package com.studproj.axiom.application.features.auth.registeruser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterUserCommand(
    @NotBlank String userName,
    @NotBlank @Email String emailAddress,
    @NotBlank @Size(min = 6) String password,
    String firstName,
    String lastName,
    @Past LocalDate dateOfBirth
) {}
