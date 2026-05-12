package com.studproj.axiom.application.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
    @NotBlank @Email String emailAddress,
    @NotBlank String password
) {}
