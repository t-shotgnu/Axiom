package com.studproj.axiom.application.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserCommand(
    @NotBlank String userName,
    @NotBlank @Email String emailAddress,
    @NotBlank String password
) {}
