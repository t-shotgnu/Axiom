package com.studproj.axiom.application.dto.query;

import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import java.time.LocalDateTime;
import java.util.UUID;

public record WorkItemDto(
    UUID id,
    Integer controlNo,
    String description,
    Integer priority,
    WorkItemType type,
    WorkItemStatus status,
    LocalDateTime dueDate,
    Integer estimatedEffort,
    UUID projectId,
    UUID authorId,
    UUID assigneeId,
    String notes
) {}
