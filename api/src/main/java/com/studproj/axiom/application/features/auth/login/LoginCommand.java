package com.studproj.axiom.application.features.auth.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
    @NotBlank @Email String emailAddress,
    @NotBlank String password
) {}
