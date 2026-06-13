package com.studproj.axiom.application.features.taskrelationships;

import com.studproj.axiom.domain.model.LinkType;
import java.util.UUID;

public record TaskRelationshipDto(
    UUID id,
    UUID sourceId,
    UUID targetId,
    LinkType linkType
) {}
