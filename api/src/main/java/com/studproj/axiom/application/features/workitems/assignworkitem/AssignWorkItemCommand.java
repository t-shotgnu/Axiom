package com.studproj.axiom.application.features.workitems.assignworkitem;

import java.util.UUID;

public record AssignWorkItemCommand(
    UUID id,
    UUID assigneeId
) {}
