package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);
    
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUserId(UUID userId);
    
    void saveAll(List<RefreshToken> refreshTokens);
}
