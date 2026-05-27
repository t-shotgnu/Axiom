package com.studproj.axiom.application.features.auth.changepassword;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordCommand(
    @NotBlank String oldPassword,
    @NotBlank String newPassword,
    @NotBlank String newPasswordConfirmation
) {}
