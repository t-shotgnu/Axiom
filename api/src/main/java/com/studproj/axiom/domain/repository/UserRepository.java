package com.studproj.axiom.domain.repository;

import com.studproj.axiom.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    void delete(UUID id);
}
