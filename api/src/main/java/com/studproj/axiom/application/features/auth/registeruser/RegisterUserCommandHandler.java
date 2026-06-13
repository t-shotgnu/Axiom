package com.studproj.axiom.application.features.auth.registeruser;

import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUserCommandHandler {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterUserResponse handle(RegisterUserCommand command) {
        userRepository.findByEmail(command.emailAddress()).ifPresent(u -> {
            throw new DomainRuleViolationException("Email already in use");
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

        return new RegisterUserResponse(token, refreshToken.getToken());
    }
}
