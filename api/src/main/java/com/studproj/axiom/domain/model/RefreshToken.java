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
public class RefreshToken {
    private UUID id;
    private String token;
    private UUID userId;
    private boolean revoked;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public void revoke() {
        this.revoked = true;
    }
}
