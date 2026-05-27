package com.studproj.axiom.application.features.workitems.updateworkitemstatus;

import com.studproj.axiom.domain.model.WorkItemStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateWorkItemStatusCommand(
    UUID id,
    @NotNull WorkItemStatus status
) {}
