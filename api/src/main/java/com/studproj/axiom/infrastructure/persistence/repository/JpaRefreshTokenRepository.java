package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaRefreshTokenRepository implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public void save(RefreshToken refreshToken) {
        var entity = RefreshTokenMapper.toEntity(refreshToken);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(RefreshTokenMapper::toDomain);
    }

    @Override
    public List<RefreshToken> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(RefreshTokenMapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<RefreshToken> refreshTokens) {
        var entities = refreshTokens.stream()
                .map(RefreshTokenMapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
    }
}
