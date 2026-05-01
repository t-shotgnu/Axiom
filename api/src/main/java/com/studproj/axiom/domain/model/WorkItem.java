package com.studproj.axiom.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItem {
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

    public void updateStatus(WorkItemStatus newStatus) {
        this.status = newStatus;
    }

    public void assignTo(UUID userId) {
        this.assigneeId = userId;
    }
}
