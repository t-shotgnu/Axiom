package com.studproj.axiom.application.features.taskrelationships.deletetaskrelationship;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DeleteTaskRelationshipCommand(
    @NotNull UUID id
) {}
