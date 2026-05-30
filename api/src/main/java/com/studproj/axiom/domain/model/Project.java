package com.studproj.axiom.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Project {
    private final UUID id;
    private final String name;
    private final String code;
    private final String description;
    private final LocalDateTime createdOn;
    private final UUID ownerId;

    public Project(UUID id, String name, String code, String description, LocalDateTime createdOn, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.createdOn = createdOn;
        this.ownerId = ownerId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
