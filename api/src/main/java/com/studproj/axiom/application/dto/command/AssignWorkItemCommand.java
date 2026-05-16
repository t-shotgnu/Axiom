package com.studproj.axiom.application.dto.command;

import java.util.UUID;

public record AssignWorkItemCommand(
    UUID assigneeId
) {}
