package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.ChangePasswordCommand;
import com.studproj.axiom.application.dto.command.LoginCommand;
import com.studproj.axiom.application.dto.command.RegisterUserCommand;
import com.studproj.axiom.application.dto.command.RefreshTokenCommand;
import com.studproj.axiom.application.dto.response.AuthResponse;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.domain.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private AuthCommandHandler handler;

    @Captor
    ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        var cmd = new RegisterUserCommand("u1", "a@b.com", "pass", "First", "Last", LocalDate.now().minusYears(20));

        when(userRepository.findByEmail(cmd.emailAddress())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(cmd.password())).thenReturn("enc");
        when(jwtService.createRefreshToken(any(UUID.class))).thenReturn(RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("r-token")
                .userId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(20))
                .revoked(false)
                .build());
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse resp = handler.register(cmd);

        assertEquals("jwt-token", resp.token());
        assertEquals("r-token", resp.refreshToken());
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        assertNotNull(refreshTokenCaptor.getValue().getToken());
    }

    @Test
    void register_whenEmailExists_shouldThrow() {
        var cmd = new RegisterUserCommand("u1", "a@b.com", "pass", "First", "Last", LocalDate.now().minusYears(20));
        when(userRepository.findByEmail(cmd.emailAddress())).thenReturn(Optional.of(User.builder().id(UUID.randomUUID()).build()));

        var ex = assertThrows(RuntimeException.class, () -> handler.register(cmd));
        assertTrue(ex.getMessage().contains("Email already in use"));
    }

    @Test
    void login_shouldReturnTokensWhenCredentialsValid() {
        var cmd = new LoginCommand("a@b.com", "pass");
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).emailAddress(cmd.emailAddress()).password("enc").build();

        when(userRepository.findByEmail(cmd.emailAddress())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(cmd.password(), user.getPassword())).thenReturn(true);
        when(jwtService.createRefreshToken(id)).thenReturn(RefreshToken.builder().id(UUID.randomUUID()).token("r2").userId(id).createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusDays(20)).build());
        when(jwtService.generateToken(user)).thenReturn("jwt2");

        AuthResponse resp = handler.login(cmd);

        assertEquals("jwt2", resp.token());
        assertEquals("r2", resp.refreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_withInvalidCredentials_shouldThrow() {
        var cmd = new LoginCommand("a@b.com", "pass");
        when(userRepository.findByEmail(cmd.emailAddress())).thenReturn(Optional.empty());

        var ex = assertThrows(RuntimeException.class, () -> handler.login(cmd));
        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    @Test
    void refreshToken_shouldIssueNewTokens() {
        UUID userId = UUID.randomUUID();
        RefreshToken old = RefreshToken.builder().id(UUID.randomUUID()).token("old").userId(userId).revoked(false).expiresAt(LocalDateTime.now().plusDays(1)).build();
        User user = User.builder().id(userId).emailAddress("x@x.com").build();

        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(old));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.createRefreshToken(userId)).thenReturn(RefreshToken.builder().id(UUID.randomUUID()).token("new-t").userId(userId).createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusDays(20)).build());
        when(jwtService.generateToken(user)).thenReturn("new-jwt");

        AuthResponse resp = handler.refreshToken(new RefreshTokenCommand("old"));

        assertEquals("new-jwt", resp.token());
        assertEquals("new-t", resp.refreshToken());
        verify(refreshTokenRepository).save(old);
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_withInvalidToken_shouldThrow() {
        when(refreshTokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        var ex = assertThrows(RuntimeException.class, () -> handler.refreshToken(new RefreshTokenCommand("bad")));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid refresh token") || ex.getMessage().toLowerCase().contains("invalid refresh token"));
    }

    @Test
    void changePassword_shouldUpdatePasswordAndRevokeTokens() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).password("old-enc").build();
        var cmd = new ChangePasswordCommand("old", "newpass", "newpass");

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "old-enc")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("new-enc");

        RefreshToken t = RefreshToken.builder().id(UUID.randomUUID()).token("t1").userId(userId).revoked(false).createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusDays(10)).build();
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(List.of(t));

        handler.changePassword(cmd);

        assertEquals("new-enc", user.getPassword());
        verify(userRepository).save(user);
        verify(refreshTokenRepository).saveAll(anyList());
    }

    @Test
    void changePassword_withWrongOldPassword_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).password("old-enc").build();
        var cmd = new ChangePasswordCommand("wrong", "newpass", "newpass");

        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-enc")).thenReturn(false);

        var ex = assertThrows(RuntimeException.class, () -> handler.changePassword(cmd));
        assertTrue(ex.getMessage().toLowerCase().contains("old password is incorrect") || ex.getMessage().toLowerCase().contains("old password is incorrect"));
    }
}
