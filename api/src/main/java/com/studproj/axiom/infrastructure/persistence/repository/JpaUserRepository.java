package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {
    private final UserJpaRepository jpaRepository;

    @Override
    public void save(User user) {
        jpaRepository.save(UserMapper.toEntity(user));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmailAddress(email).map(UserMapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
