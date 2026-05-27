package com.studproj.axiom.application.dto.command;

import com.studproj.axiom.domain.model.WorkItemType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateWorkItemCommand(
        String description,
        Integer priority,
        WorkItemType type,
        LocalDateTime dueDate,
        Integer estimatedEffort,
        String notes,
        UUID assigneeId
) {}