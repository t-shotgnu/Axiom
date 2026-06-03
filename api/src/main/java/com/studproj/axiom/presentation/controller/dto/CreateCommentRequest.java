package com.studproj.axiom.presentation.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(@NotBlank String text) {
}