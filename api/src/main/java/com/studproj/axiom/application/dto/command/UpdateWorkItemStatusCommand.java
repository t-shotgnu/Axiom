package com.studproj.axiom.application.dto.command;

import com.studproj.axiom.domain.model.WorkItemStatus;

public record UpdateWorkItemStatusCommand(
    WorkItemStatus status
) {}
