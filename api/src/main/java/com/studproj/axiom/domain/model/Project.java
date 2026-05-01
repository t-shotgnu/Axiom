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
public class Project {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private LocalDateTime createdOn;
    private UUID ownerId;
}
