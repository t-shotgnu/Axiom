package com.studproj.axiom.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordCommand(
    @NotBlank String oldPassword,
    @NotBlank String newPassword,
    @NotBlank String newPasswordConfirmation
) {}
