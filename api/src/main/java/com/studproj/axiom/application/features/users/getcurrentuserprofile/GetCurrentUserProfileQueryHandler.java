package com.studproj.axiom.application.features.users.getcurrentuserprofile;

import com.studproj.axiom.application.features.users.UserDto;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentUserProfileQueryHandler {
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public UserDto handle(GetCurrentUserProfileQuery query) {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        return userRepository.findById(userId)
                .map(user -> new UserDto(user.getId(), user.getUserName(), user.getEmailAddress(), user.getFirstName(), user.getLastName(), user.getDateOfBirth()))
                .orElseThrow(() -> new DomainRuleViolationException("User not found"));
    }
}
