package com.studproj.axiom.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class WorkItem {
    private final UUID id;
    private final Integer controlNo;
    private String description;
    private Integer priority;
    private WorkItemType type;
    private WorkItemStatus status;
    private LocalDateTime dueDate;
    private Integer estimatedEffort;
    private final UUID projectId;
    private final UUID authorId;
    private UUID assigneeId;
    private String notes;

    public WorkItem(UUID id, Integer controlNo, String description, Integer priority, WorkItemType type, 
                    WorkItemStatus status, LocalDateTime dueDate, Integer estimatedEffort, UUID projectId, 
                    UUID authorId, UUID assigneeId, String notes) {
        this.id = id;
        this.controlNo = controlNo;
        this.description = description;
        this.priority = priority;
        this.type = type;
        this.status = status;
        this.dueDate = dueDate;
        this.estimatedEffort = estimatedEffort;
        this.projectId = projectId;
        this.authorId = authorId;
        this.assigneeId = assigneeId;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public Integer getControlNo() {
        return controlNo;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPriority() {
        return priority;
    }

    public WorkItemType getType() {
        return type;
    }

    public WorkItemStatus getStatus() {
        return status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Integer getEstimatedEffort() {
        return estimatedEffort;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public String getNotes() {
        return notes;
    }

    public void updateStatus(WorkItemStatus newStatus) {
        this.status = newStatus;
    }

    public void assignTo(UUID userId) {
        this.assigneeId = userId;
    }

    public void updateDetails(String description, Integer priority, WorkItemType type, LocalDateTime dueDate, Integer estimatedEffort) {
        this.description = description;
        this.priority = priority;
        this.type = type;
        this.dueDate = dueDate;
        this.estimatedEffort = estimatedEffort;
    }

    public void updateNotes(String newNotes) {
        this.notes = newNotes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Integer controlNo;
        private String description;
        private Integer priority;
        private WorkItemType type;
        private WorkItemStatus status;
        private LocalDateTime dueDate;
        private Integer estimatedEffort;
        private UUID projectId;
        private UUID authorId;
        private UUID assigneeId;
        private String notes;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder controlNo(Integer controlNo) {
            this.controlNo = controlNo;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public Builder type(WorkItemType type) {
            this.type = type;
            return this;
        }

        public Builder status(WorkItemStatus status) {
            this.status = status;
            return this;
        }

        public Builder dueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder estimatedEffort(Integer estimatedEffort) {
            this.estimatedEffort = estimatedEffort;
            return this;
        }

        public Builder projectId(UUID projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder authorId(UUID authorId) {
            this.authorId = authorId;
            return this;
        }

        public Builder assigneeId(UUID assigneeId) {
            this.assigneeId = assigneeId;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public WorkItem build() {
            return new WorkItem(id, controlNo, description, priority, type, status, dueDate, estimatedEffort, projectId, authorId, assigneeId, notes);
        }
    }
}
