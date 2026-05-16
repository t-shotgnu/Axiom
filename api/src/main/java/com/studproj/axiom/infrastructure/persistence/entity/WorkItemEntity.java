package com.studproj.axiom.infrastructure.persistence.entity;

import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemEntity {
    @Id
    private UUID id;
    private Integer controlNo;
    private String description;
    private Integer priority;
    
    @Enumerated(EnumType.STRING)
    private WorkItemType type;
    
    @Enumerated(EnumType.STRING)
    private WorkItemStatus status;
    
    private LocalDateTime dueDate;
    private Integer estimatedEffort;
    private UUID projectId;
    private UUID authorId;
    private UUID assigneeId;
    
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String notes;
}
