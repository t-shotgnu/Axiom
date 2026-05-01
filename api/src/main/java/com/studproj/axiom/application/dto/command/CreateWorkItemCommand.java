package com.studproj.axiom.application.dto.command;

import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateWorkItemCommand(
    @NotNull Integer controlNo,
    @NotBlank String description,
    Integer priority,
    @NotNull WorkItemType type,
    @NotNull WorkItemStatus status,
    LocalDateTime dueDate,
    Integer estimatedEffort,
    @NotNull UUID projectId,
    @NotNull UUID authorId,
    UUID assigneeId
) {}
