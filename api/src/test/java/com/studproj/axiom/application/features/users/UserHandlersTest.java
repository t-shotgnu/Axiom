package com.studproj.axiom.application.features.users;

import com.studproj.axiom.application.features.users.getcurrentuserprofile.GetCurrentUserProfileQuery;
import com.studproj.axiom.application.features.users.getcurrentuserprofile.GetCurrentUserProfileQueryHandler;
import com.studproj.axiom.application.features.users.updateuserprofile.UpdateUserProfileCommand;
import com.studproj.axiom.application.features.users.updateuserprofile.UpdateUserProfileCommandHandler;
import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserHandlersTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Nested
    class GetCurrentUserProfileTests {
        @Mock private UserRepository userRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private GetCurrentUserProfileQueryHandler handler;

        @Test
        void shouldReturnCurrentUserProfile() {
            User user = User.builder().id(USER_ID).userName("john").emailAddress("john@e.com")
                    .password("p").firstName("John").lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .createdOn(LocalDateTime.now()).active(true).build();

            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            UserDto result = handler.handle(new GetCurrentUserProfileQuery());

            assertThat(result.id()).isEqualTo(USER_ID);
            assertThat(result.userName()).isEqualTo("john");
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new GetCurrentUserProfileQuery()))
                    .isInstanceOf(DomainRuleViolationException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    class UpdateUserProfileTests {
        @Mock private UserRepository userRepository;
        @Mock private AuthenticatedUserProvider authenticatedUserProvider;
        @InjectMocks private UpdateUserProfileCommandHandler handler;

        private User user;

        @BeforeEach
        void setUp() {
            user = User.builder().id(USER_ID).userName("john").emailAddress("john@e.com")
                    .password("p").firstName("John").lastName("Doe")
                    .createdOn(LocalDateTime.now()).active(true).build();
        }

        @Test
        void shouldUpdateProfileSuccessfully() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            handler.handle(new UpdateUserProfileCommand("Jane", "Smith", LocalDate.of(1995, 5, 20)));

            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 20));
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> handler.handle(new UpdateUserProfileCommand("J", "S", null)))
                    .isInstanceOf(AuthenticationRequiredException.class)
                    .hasMessage("Authentication invalid");
        }

        @Test
        void shouldAllowNullFields() {
            when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            handler.handle(new UpdateUserProfileCommand(null, null, null));

            assertThat(user.getFirstName()).isNull();
            assertThat(user.getLastName()).isNull();
            verify(userRepository).save(user);
        }
    }
}
