package com.studproj.axiom.application.features.auth.refreshtoken;

import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenCommandHandler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public RefreshTokenResponse handle(RefreshTokenCommand command) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new AuthenticationRequiredException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired(LocalDateTime.now())) {
            throw new AuthenticationRequiredException("Refresh token is invalid or expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new AuthenticationRequiredException("User not found"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = jwtService.createRefreshToken(user.getId());
        refreshTokenRepository.save(newRefreshToken);

        String newToken = jwtService.generateToken(user);
        return new RefreshTokenResponse(newToken, newRefreshToken.getToken());
    }
}
