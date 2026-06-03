package com.studproj.axiom.application.features.comments;

import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID workItemId,
        UUID authorId,
        @Nullable
        String author,
        String text,
        LocalDateTime createdOn
) {
}
