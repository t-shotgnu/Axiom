package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.UserDto;
import com.studproj.axiom.domain.exception.BadRequestException;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.infrastructure.persistence.repository.UserJpaRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserQueryHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserJpaRepository jpaRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private UserQueryHandler handler;

    @Test
    void getUserById_returnsDtoWhenPresent() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).userName("u").emailAddress("a@b.com").firstName("F").lastName("L").dateOfBirth(LocalDate.of(1990,1,1)).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<UserDto> dto = handler.getUserById(id);

        assertThat(dto).isPresent();
        assertThat(dto.get().emailAddress()).isEqualTo("a@b.com");
    }

    @Test
    void getAllUsers_mapsEntitiesToDtos() {
        User u1 = User.builder().id(UUID.randomUUID()).userName("u1").emailAddress("x@x.com").firstName("A").lastName("B").dateOfBirth(LocalDate.of(1990,1,1)).build();
        when(jpaRepository.findAll()).thenReturn(List.of(com.studproj.axiom.infrastructure.persistence.entity.UserEntity.builder().id(u1.getId()).userName(u1.getUserName()).emailAddress(u1.getEmailAddress()).firstName(u1.getFirstName()).lastName(u1.getLastName()).dateOfBirth(u1.getDateOfBirth()).build()));

        List<UserDto> list = handler.getAllUsers();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).emailAddress()).isEqualTo("x@x.com");
    }

    @Test
    void getCurrentUserProfile_returnsDtoOrThrows() {
        UUID id = UUID.randomUUID();
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(id);

        User user = User.builder().id(id).emailAddress("me@me.com").userName("me").firstName("F").lastName("L").dateOfBirth(LocalDate.of(1991,2,3)).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var dto = handler.getCurrentUserProfile();
        assertThat(dto.emailAddress()).isEqualTo("me@me.com");

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> handler.getCurrentUserProfile()).isInstanceOf(BadRequestException.class);
    }
}
