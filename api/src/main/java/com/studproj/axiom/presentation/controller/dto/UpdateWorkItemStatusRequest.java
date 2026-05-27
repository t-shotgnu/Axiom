package com.studproj.axiom.presentation.controller.dto;

import com.studproj.axiom.domain.model.WorkItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkItemStatusRequest(@NotNull WorkItemStatus status) {}
