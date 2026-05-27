package com.studproj.axiom.application.features.users.getuserbyid;

import com.studproj.axiom.application.features.users.UserDto;
import com.studproj.axiom.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserByIdQueryHandler {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<UserDto> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.id())
                .map(user -> new UserDto(user.getId(), user.getUserName(), user.getEmailAddress(), user.getFirstName(), user.getLastName(), user.getDateOfBirth()));
    }
}
