package com.studproj.axiom.infrastructure.security;

import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {
    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 20;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmailAddress())
                .claim("userId", user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, String username) {
        try {
            String extracted = extractUsername(token);

            return Objects.equals(extracted, username) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public String generateRefreshTokenString() {
        byte[] bytes = new byte[64];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public RefreshToken createRefreshToken(UUID userId) {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(generateRefreshTokenString())
                .userId(userId)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS))
                .build();
    }

    private boolean isExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}