package com.studproj.axiom.application.features.projects.getallprojects;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectDto(
    UUID id,
    String name,
    String code,
    String description,
    LocalDateTime createdOn,
    UUID ownerId,
    String ownerName
) {}
