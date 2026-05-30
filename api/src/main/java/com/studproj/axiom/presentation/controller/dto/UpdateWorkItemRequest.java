package com.studproj.axiom.presentation.controller.dto;

import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.domain.model.WorkItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateWorkItemRequest(
    @NotBlank String description,
    Integer priority,
    @NotNull WorkItemType type,
    WorkItemStatus status,
    LocalDateTime dueDate,
    Integer estimatedEffort,
    UUID assigneeId,
    String notes
) {}
