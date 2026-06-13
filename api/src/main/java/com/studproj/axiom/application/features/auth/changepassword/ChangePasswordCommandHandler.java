package com.studproj.axiom.application.features.auth.changepassword;

import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.model.RefreshToken;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.RefreshTokenRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordCommandHandler {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(ChangePasswordCommand command) {
        if (!command.newPassword().equals(command.newPasswordConfirmation())) {
            throw new DomainRuleViolationException("New passwords do not match");
        }

        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationRequiredException("Authentication invalid"));

        if (!passwordEncoder.matches(command.oldPassword(), user.getPassword())) {
            throw new DomainRuleViolationException("Old password is incorrect");
        }

        if (passwordEncoder.matches(command.newPassword(), user.getPassword())) {
            throw new DomainRuleViolationException("New password cannot be the same as the current password");
        }

        String encodedNewPassword = passwordEncoder.encode(command.newPassword());
        user.changePassword(encodedNewPassword);
        userRepository.save(user);

        List<RefreshToken> userRefreshTokens = refreshTokenRepository.findByUserId(userId);
        userRefreshTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(userRefreshTokens);
    }
}
