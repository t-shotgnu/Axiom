package com.studproj.axiom.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
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
@Table(name = "attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentEntity {
    @Id
    private UUID id;
    private UUID workItemId;
    private UUID uploadedBy;
    private String objectKey;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadedOn;
}
