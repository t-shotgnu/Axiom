package com.studproj.axiom.application.dto.query;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectDto(
    UUID id,
    String name,
    String code,
    String description,
    LocalDateTime createdOn,
    UUID ownerId
) {}
