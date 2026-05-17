package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.ChangePasswordCommand;
import com.studproj.axiom.application.dto.command.RefreshTokenCommand;
import com.studproj.axiom.application.dto.command.RegisterUserCommand;
import com.studproj.axiom.application.dto.command.LoginCommand;
import com.studproj.axiom.application.dto.response.AuthResponse;
import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.exception.UnauthorizedException;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.domain.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthCommandHandler {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public AuthResponse register(RegisterUserCommand command) {
        userRepository.findByEmail(command.emailAddress()).ifPresent(u -> {
            throw new BadRequestException("Email already in use");
        });

        User user = User.builder()
                .id(UUID.randomUUID())
                .userName(command.userName())
                .emailAddress(command.emailAddress())
                .password(passwordEncoder.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .dateOfBirth(command.dateOfBirth())
                .createdOn(LocalDateTime.now())
                .active(true)
                .build();

        userRepository.save(user);

        RefreshToken refreshToken = jwtService.createRefreshToken(user.getId());
        refreshTokenRepository.save(refreshToken);

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse login(LoginCommand command) {
        User user = userRepository.findByEmail(command.emailAddress())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        user.updateLoginTime();
        userRepository.save(user);

        RefreshToken refreshToken = jwtService.createRefreshToken(user.getId());
        refreshTokenRepository.save(refreshToken);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenCommand command) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = jwtService.createRefreshToken(user.getId());
        refreshTokenRepository.save(newRefreshToken);

        String newToken = jwtService.generateToken(user);
        return new AuthResponse(newToken, newRefreshToken.getToken());
    }

    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        if (!command.newPassword().equals(command.newPasswordConfirmation())) {
            throw new BadRequestException("New passwords do not match");
        }

        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Authentication invalid"));

        if (!passwordEncoder.matches(command.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (passwordEncoder.matches(command.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the current password");
        }

        String encodedNewPassword = passwordEncoder.encode(command.newPassword());
        user.changePassword(encodedNewPassword);
        userRepository.save(user);

        List<RefreshToken> userRefreshTokens = refreshTokenRepository.findByUserId(userId);
        userRefreshTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(userRefreshTokens);
    }
}
