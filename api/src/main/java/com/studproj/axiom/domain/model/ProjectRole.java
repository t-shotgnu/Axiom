package com.studproj.axiom.domain.model;

import java.util.UUID;

public class ProjectRole {
    private final UUID id;
    private final ProjectRoleType type;

    public ProjectRole(UUID id, ProjectRoleType type) {
        this.id = id;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public ProjectRoleType getType() {
        return type;
    }
}