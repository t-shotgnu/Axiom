package com.studproj.axiom.domain.service;

import com.studproj.axiom.domain.model.User;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(
            String token,
            String username
    );
}