package com.studproj.axiom.application.features.workitems.updateworkitem;

import com.studproj.axiom.domain.model.WorkItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateWorkItemCommand(
    UUID id,
    @NotBlank String description,
    Integer priority,
    @NotNull WorkItemType type,
    LocalDateTime dueDate,
    Integer estimatedEffort,
    UUID assigneeId,
    String notes
) {}
