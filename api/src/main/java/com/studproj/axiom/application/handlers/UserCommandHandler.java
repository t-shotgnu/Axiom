package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.CreateUserCommand;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCommandHandler {
    private final UserRepository userRepository;

    @Transactional
    public UUID createUser(CreateUserCommand command) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .userName(command.userName())
                .emailAddress(command.emailAddress())
                .password(command.password())
                .createdOn(LocalDateTime.now())
                .active(true)
                .build();

        userRepository.save(user);
        return user.getId();
    }
}

