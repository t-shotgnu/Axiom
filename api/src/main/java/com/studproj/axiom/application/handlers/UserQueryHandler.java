package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.UserDto;
import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import com.studproj.axiom.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQueryHandler {
    private final UserRepository userRepository;
    private final UserJpaRepository jpaRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(user -> new UserDto(user.getId(), user.getUserName(), user.getEmailAddress(), user.getFirstName(), user.getLastName(), user.getDateOfBirth()));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return jpaRepository.findAll().stream()
                .map(entity -> new UserDto(entity.getId(), entity.getUserName(), entity.getEmailAddress(), entity.getFirstName(), entity.getLastName(), entity.getDateOfBirth()))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile() {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        return userRepository.findById(userId)
                .map(user -> new UserDto(user.getId(), user.getUserName(), user.getEmailAddress(), user.getFirstName(), user.getLastName(), user.getDateOfBirth()))
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
