package com.studproj.axiom.application.features.taskrelationships.createtaskrelationship;

import com.studproj.axiom.domain.model.LinkType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateTaskRelationshipCommand(
    @NotNull UUID sourceId,
    @NotNull UUID targetId,
    @NotNull LinkType linkType
) {}
