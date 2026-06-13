package com.studproj.axiom.application.features.auth;

import com.studproj.axiom.application.features.auth.changepassword.ChangePasswordCommand;
import com.studproj.axiom.application.features.auth.changepassword.ChangePasswordCommandHandler;
import com.studproj.axiom.application.features.auth.login.LoginCommand;
import com.studproj.axiom.application.features.auth.login.LoginCommandHandler;
import com.studproj.axiom.application.features.auth.login.LoginResponse;
import com.studproj.axiom.application.features.auth.refreshtoken.RefreshTokenCommand;
import com.studproj.axiom.application.features.auth.refreshtoken.RefreshTokenCommandHandler;
import com.studproj.axiom.application.features.auth.refreshtoken.RefreshTokenResponse;
import com.studproj.axiom.application.features.auth.registeruser.RegisterUserCommand;
import com.studproj.axiom.application.features.auth.registeruser.RegisterUserCommandHandler;
import com.studproj.axiom.application.features.auth.registeruser.RegisterUserResponse;
import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.domain.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthHandlersTest {

    @Nested
    class LoginTests {
        @Mock private UserRepository userRepository;
        @Mock private RefreshTokenRepository refreshTokenRepository;
        @Mock private PasswordEncoder passwordEncoder;
        @Mock private JwtService jwtService;
        @InjectMocks private LoginCommandHandler handler;

        private User user;

        @BeforeEach
        void setUp() {
            user = User.builder()
                    .id(UUID.randomUUID())
                    .userName("testuser")
                    .emailAddress("test@example.com")
                    .password("encoded-password")
                    .active(true)
                    .createdOn(LocalDateTime.now())
                    .build();
        }

        @Test
        void shouldLoginSuccessfully() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("access-token");
            when(jwtService.createRefreshToken(user.getId()))
                    .thenReturn(new RefreshToken(UUID.randomUUID(), "refresh-token", user.getId(), false, LocalDateTime.now().plusDays(7), LocalDateTime.now()));

            LoginResponse response = handler.handle(new LoginCommand("test@example.com", "password123"));

            assertThat(response.token()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowWhenEmailNotFound() {
            when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new LoginCommand("wrong@example.com", "password")))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        void shouldThrowWhenPasswordDoesNotMatch() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new LoginCommand("test@example.com", "wrong-password")))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("Invalid email or password");
        }
    }

    @Nested
    class RegisterTests {
        @Mock private UserRepository userRepository;
        @Mock private RefreshTokenRepository refreshTokenRepository;
        @Mock private PasswordEncoder passwordEncoder;
        @Mock private JwtService jwtService;
        @InjectMocks private RegisterUserCommandHandler handler;

        @Test
        void shouldRegisterSuccessfully() {
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password123")).thenReturn("encoded");
            when(jwtService.generateToken(any(User.class))).thenReturn("token");
            when(jwtService.createRefreshToken(any(UUID.class)))
                    .thenReturn(new RefreshToken(UUID.randomUUID(), "rt", UUID.randomUUID(), false, LocalDateTime.now().plusDays(7), LocalDateTime.now()));

            RegisterUserResponse response = handler.handle(new RegisterUserCommand(
                    "newuser", "new@example.com", "password123", "John", "Doe", null));

            assertThat(response.token()).isEqualTo("token");
            verify(userRepository).save(any(User.class));
        }

        @Test
        void shouldThrowWhenEmailAlreadyInUse() {
            User existing = User.builder().id(UUID.randomUUID()).emailAddress("taken@example.com")
                    .userName("u").password("p").createdOn(LocalDateTime.now()).active(true).build();
            when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> handler.handle(new RegisterUserCommand(
                    "user", "taken@example.com", "pass123", null, null, null)))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("Email already in use");
        }
    }

    @Nested
    class RefreshTokenTests {
        @Mock private RefreshTokenRepository refreshTokenRepository;
        @Mock private UserRepository userRepository;
        @Mock private JwtService jwtService;
        @InjectMocks private RefreshTokenCommandHandler handler;

        private UUID userId;
        private User user;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            user = User.builder().id(userId).userName("u").emailAddress("e@e.com")
                    .password("p").createdOn(LocalDateTime.now()).active(true).build();
        }

        @Test
        void shouldRefreshTokenSuccessfully() {
            RefreshToken token = new RefreshToken(UUID.randomUUID(), "valid-rt", userId, false, LocalDateTime.now().plusDays(1), LocalDateTime.now());
            when(refreshTokenRepository.findByToken("valid-rt")).thenReturn(Optional.of(token));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(jwtService.generateToken(user)).thenReturn("new-access");
            when(jwtService.createRefreshToken(userId))
                    .thenReturn(new RefreshToken(UUID.randomUUID(), "new-rt", userId, false, LocalDateTime.now().plusDays(7), LocalDateTime.now()));

            RefreshTokenResponse response = handler.handle(new RefreshTokenCommand("valid-rt"));

            assertThat(response.token()).isEqualTo("new-access");
            assertThat(response.refreshToken()).isEqualTo("new-rt");
            assertThat(token.isRevoked()).isTrue();
        }

        @Test
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand("invalid")))
                    .isInstanceOf(AuthenticationRequiredException.class)
                    .hasMessage("Invalid refresh token");
        }

        @Test
        void shouldThrowWhenTokenIsRevoked() {
            RefreshToken revoked = new RefreshToken(UUID.randomUUID(), "revoked-rt", userId, true, LocalDateTime.now().plusDays(1), LocalDateTime.now());
            when(refreshTokenRepository.findByToken("revoked-rt")).thenReturn(Optional.of(revoked));

            assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand("revoked-rt")))
                    .isInstanceOf(AuthenticationRequiredException.class)
                    .hasMessage("Refresh token is invalid or expired");
        }

        @Test
        void shouldThrowWhenTokenIsExpired() {
            RefreshToken expired = new RefreshToken(UUID.randomUUID(), "expired-rt", userId, false, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(2));
            when(refreshTokenRepository.findByToken("expired-rt")).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand("expired-rt")))
                    .isInstanceOf(AuthenticationRequiredException.class)
                    .hasMessage("Refresh token is invalid or expired");
        }
    }

    @Nested
    class ChangePasswordTests {
        @Mock private UserRepository userRepository;
        @Mock private RefreshTokenRepository refreshTokenRepository;
        @Mock private PasswordEncoder passwordEncoder;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private ChangePasswordCommandHandler handler;

        private UUID userId;
        private User user;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            user = User.builder().id(userId).userName("u").emailAddress("e@e.com")
                    .password("encoded-old").createdOn(LocalDateTime.now()).active(true).build();
        }

        @Test
        void shouldChangePasswordSuccessfully() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldpass", "encoded-old")).thenReturn(true);
            when(passwordEncoder.matches("newpass", "encoded-old")).thenReturn(false);
            when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");
            when(refreshTokenRepository.findByUserId(userId)).thenReturn(List.of());

            handler.handle(new ChangePasswordCommand("oldpass", "newpass", "newpass"));

            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowWhenPasswordsDoNotMatch() {
            assertThatThrownBy(() -> handler.handle(new ChangePasswordCommand("old", "new1", "new2")))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("New passwords do not match");
        }

        @Test
        void shouldThrowWhenOldPasswordIsIncorrect() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongold", "encoded-old")).thenReturn(false);

            assertThatThrownBy(() -> handler.handle(new ChangePasswordCommand("wrongold", "newpass", "newpass")))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("Old password is incorrect");
        }

        @Test
        void shouldThrowWhenNewPasswordSameAsCurrent() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldpass", "encoded-old")).thenReturn(true);
            when(passwordEncoder.matches("oldpass", "encoded-old")).thenReturn(true);

            assertThatThrownBy(() -> handler.handle(new ChangePasswordCommand("oldpass", "oldpass", "oldpass")))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("New password cannot be the same as the current password");
        }
    }
}
