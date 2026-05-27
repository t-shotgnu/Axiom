package com.studproj.axiom.application.features.workitems.updateworkitemnotes;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateWorkItemNotesCommand(
    UUID id,
    @NotBlank String notes
) {}
