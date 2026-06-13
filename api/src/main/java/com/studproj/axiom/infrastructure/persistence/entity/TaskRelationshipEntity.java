package com.studproj.axiom.infrastructure.persistence.entity;

import com.studproj.axiom.domain.model.LinkType;
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

import java.util.UUID;

@Entity
@Table(name = "task_relationships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRelationshipEntity {
    @Id
    private UUID id;
    
    private UUID sourceId;
    
    private UUID targetId;
    
    @Enumerated(EnumType.STRING)
    private LinkType linkType;
}
