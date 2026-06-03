package com.studproj.axiom.application.features.comments.createcomment;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCommentCommand(
        UUID workItemId,
        @NotBlank String text
) {
}
