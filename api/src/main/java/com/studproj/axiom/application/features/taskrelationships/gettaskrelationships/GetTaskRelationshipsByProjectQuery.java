package com.studproj.axiom.application.features.taskrelationships.gettaskrelationships;

import java.util.UUID;

public record GetTaskRelationshipsByProjectQuery(
    UUID projectId
) {}
