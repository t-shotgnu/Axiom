package com.studproj.axiom.application.features.auth.login;

import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginCommandHandler {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse handle(LoginCommand command) {
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
        return new LoginResponse(token, refreshToken.getToken());
    }
}
