package com.studproj.axiom.application.features.users.updateuserprofile;

import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileCommandHandler {
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void handle(UpdateUserProfileCommand command) {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationRequiredException("Authentication invalid"));

        user.updateProfile(command.firstName(), command.lastName(), command.dateOfBirth());
        userRepository.save(user);
    }
}
