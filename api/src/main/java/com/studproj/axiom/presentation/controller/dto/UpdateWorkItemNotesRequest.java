package com.studproj.axiom.presentation.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateWorkItemNotesRequest(@NotBlank String notes) {}
