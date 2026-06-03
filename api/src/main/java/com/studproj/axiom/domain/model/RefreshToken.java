package com.studproj.axiom.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshToken {
    private final UUID id;
    private final String token;
    private final UUID userId;
    private boolean revoked;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public RefreshToken(UUID id, String token, UUID userId, boolean revoked, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.revoked = revoked;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public void revoke() {
        this.revoked = true;
    }
}
