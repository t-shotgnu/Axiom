package com.studproj.axiom.infrastructure.persistence.entity;

import jakarta.persistence.Column;
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
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    private UUID id;
    private String userName;
    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;
    private String password;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private boolean active;
    private LocalDateTime lastLogin;
}
