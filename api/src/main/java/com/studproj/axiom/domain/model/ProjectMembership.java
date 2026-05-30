package com.studproj.axiom.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectMembership {
    private final UUID id;
    private final UUID projectId;
    private final UUID userId;
    private UUID roleId;
    private final LocalDateTime createdOn;

    public ProjectMembership(UUID id, UUID projectId, UUID userId, UUID roleId, LocalDateTime createdOn) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.roleId = roleId;
        this.createdOn = createdOn;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void changeRole(UUID newRoleId) {
        if (newRoleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        this.roleId = newRoleId;
    }
}