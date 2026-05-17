package com.studproj.axiom.domain.service;

import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;

import java.util.UUID;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, String username);

    String generateRefreshTokenString();

    RefreshToken createRefreshToken(UUID userId);
}