package com.studproj.axiom.application.features.users.getallusers;

import com.studproj.axiom.application.features.users.UserDto;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllUsersQueryHandler {
    private final UserJpaRepository jpaRepository;

    @Transactional(readOnly = true)
    public List<UserDto> handle(GetAllUsersQuery query) {
        return jpaRepository.findAll().stream()
                .map(entity -> new UserDto(entity.getId(), entity.getUserName(), entity.getEmailAddress(), entity.getFirstName(), entity.getLastName(), entity.getDateOfBirth()))
                .toList();
    }
}
