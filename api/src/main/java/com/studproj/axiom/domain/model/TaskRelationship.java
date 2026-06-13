package com.studproj.axiom.domain.model;

import java.util.UUID;

public class TaskRelationship {
    private final UUID id;
    private final UUID sourceId;
    private final UUID targetId;
    private final LinkType linkType;

    public TaskRelationship(UUID id, UUID sourceId, UUID targetId, LinkType linkType) {
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.linkType = linkType;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID sourceId;
        private UUID targetId;
        private LinkType linkType;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder sourceId(UUID sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder targetId(UUID targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder linkType(LinkType linkType) {
            this.linkType = linkType;
            return this;
        }

        public TaskRelationship build() {
            return new TaskRelationship(id, sourceId, targetId, linkType);
        }
    }
}
