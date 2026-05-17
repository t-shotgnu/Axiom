package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.command.UpdateUserProfileCommand;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private UserCommandHandler handler;

    @Test
    void updateUserProfile_shouldUpdateAndSave() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).firstName("A").lastName("B").build();
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var cmd = new UpdateUserProfileCommand("NewFirst", "NewLast", LocalDate.of(1990,1,1));
        handler.updateUserProfile(cmd);

        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfile_whenUserMissing_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        var cmd = new UpdateUserProfileCommand("NewFirst", "NewLast", LocalDate.of(1990,1,1));
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> handler.updateUserProfile(cmd));
    }
}
