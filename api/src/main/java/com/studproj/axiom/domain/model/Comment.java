package com.studproj.axiom.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {
    private final UUID id;
    private final UUID workItemId;
    private final UUID authorId;
    private final String text;
    private final LocalDateTime createdOn;

    public Comment(UUID id, UUID workItemId, UUID authorId, String text, LocalDateTime createdOn) {
        this.id = id;
        this.workItemId = workItemId;
        this.authorId = authorId;
        this.text = text;
        this.createdOn = createdOn;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkItemId() {
        return workItemId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}