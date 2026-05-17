package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.UpdateUserProfileCommand;
import com.studproj.axiom.domain.exception.UnauthorizedException;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCommandHandler {
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void updateUserProfile(UpdateUserProfileCommand command) {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Authentication invalid"));

        user.updateProfile(command.firstName(), command.lastName(), command.dateOfBirth());
        userRepository.save(user);
    }
}

