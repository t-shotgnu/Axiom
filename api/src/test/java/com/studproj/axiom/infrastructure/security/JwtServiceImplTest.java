package com.studproj.axiom.infrastructure.security;

import com.studproj.axiom.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private static final String SECRET = "c3VwZXJTZWNyZXRLZXlGb3JKV1QxMjM0NTY3ODkwMTIzNDU2Nzg5MA==";

    @Test
    void generateTokenEmbedsTheUserEmailAsSubject() {
        JwtServiceImpl service = jwtService(60_000);
        User user = User.builder()
                .id(UUID.randomUUID())
                .emailAddress("user@example.com")
                .build();

        String token = service.generateToken(user);

        assertThat(service.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(service.isTokenValid(token, "user@example.com")).isTrue();
        assertThat(service.isTokenValid(token, "other@example.com")).isFalse();
    }

    @Test
    void expiredTokensAreNotValid() {
        JwtServiceImpl service = jwtService(-1_000);
        User user = User.builder()
                .id(UUID.randomUUID())
                .emailAddress("user@example.com")
                .build();

        String token = service.generateToken(user);

        assertThat(service.isTokenValid(token, "user@example.com")).isFalse();
    }

    private JwtServiceImpl jwtService(long expiration) {
        JwtServiceImpl service = new JwtServiceImpl();
        ReflectionTestUtils.setField(service, "secret", SECRET);
        ReflectionTestUtils.setField(service, "expiration", expiration);
        return service;
    }

    @Test
    void generateRefreshTokenStringProducesUrlSafeToken() {
        JwtServiceImpl service = jwtService(60_000);

        String refresh = service.generateRefreshTokenString();

        assertThat(refresh).isNotBlank();
        // URL-safe Base64 should not contain + or / or =
        assertThat(refresh).doesNotContain("+");
        assertThat(refresh).doesNotContain("/");
        assertThat(refresh).doesNotContain("=");
    }

    @Test
    void createRefreshTokenHasProperFieldsAndExpiration() {
        JwtServiceImpl service = jwtService(60_000);
        UUID userId = UUID.randomUUID();

        var rt = service.createRefreshToken(userId);

        assertThat(rt).isNotNull();
        assertThat(rt.getUserId()).isEqualTo(userId);
        assertThat(rt.getToken()).isNotBlank();
        assertThat(rt.getCreatedAt()).isNotNull();
        assertThat(rt.getExpiresAt()).isAfter(rt.getCreatedAt());
    }

    @Test
    void tokenSignedWithDifferentSecretIsNotValid() {
        JwtServiceImpl s1 = jwtService(60_000);
        JwtServiceImpl s2 = jwtService(60_000);

        // change secret on second service
        ReflectionTestUtils.setField(s2, "secret", "dGVzdC1vdGhlci1zZWNyZXQ=");

        User user = User.builder().id(UUID.randomUUID()).emailAddress("user@example.com").build();
        String token = s1.generateToken(user);

        assertThat(s1.isTokenValid(token, "user@example.com")).isTrue();
        assertThat(s2.isTokenValid(token, "user@example.com")).isFalse();
    }
}
