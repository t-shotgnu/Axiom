package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.RegisterUserCommand;
import com.studproj.axiom.application.dto.command.LoginCommand;
import com.studproj.axiom.application.dto.response.AuthResponse;
import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.model.User;
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
public class AuthCommandHandler {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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
                .createdOn(LocalDateTime.now())
                .active(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getId(), user.getUserName(), user.getEmailAddress());
    }

    public AuthResponse login(LoginCommand command) {
        User user = userRepository.findByEmail(command.emailAddress())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        user.updateLoginTime();
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUserName(), user.getEmailAddress());
    }
}
