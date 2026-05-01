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
public class User {
    private UUID id;
    private String userName;
    private String emailAddress;
    private String password;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private boolean active;
    private LocalDateTime lastLogin;

    public void activate() {
        active = true;
        modifiedOn = LocalDateTime.now();
    }

    public void deactivate() {
        active = false;
        modifiedOn = LocalDateTime.now();
    }

    public void updateLoginTime() {
        lastLogin = LocalDateTime.now();
    }
}
