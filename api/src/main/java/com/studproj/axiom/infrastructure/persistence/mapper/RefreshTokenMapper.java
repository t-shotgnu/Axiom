package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.infrastructure.persistence.entity.RefreshTokenEntity;

public class RefreshTokenMapper {
    public static RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) return null;
        return new RefreshToken(
                entity.getId(),
                entity.getToken(),
                entity.getUserId(),
                entity.isRevoked(),
                entity.getExpiresAt(),
                entity.getCreatedAt()
        );
    }

    public static RefreshTokenEntity toEntity(RefreshToken domain) {
        if (domain == null) return null;
        return RefreshTokenEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .userId(domain.getUserId())
                .revoked(domain.isRevoked())
                .expiresAt(domain.getExpiresAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
