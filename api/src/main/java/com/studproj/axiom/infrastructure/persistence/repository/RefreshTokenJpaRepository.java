package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);
    
    List<RefreshTokenEntity> findByUserId(UUID userId);
}
