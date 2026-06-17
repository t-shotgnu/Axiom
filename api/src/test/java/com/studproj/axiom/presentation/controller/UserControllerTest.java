package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.users.UserDto;
import com.studproj.axiom.application.features.users.getallusers.GetAllUsersQuery;
import com.studproj.axiom.application.features.users.getallusers.GetAllUsersQueryHandler;
import com.studproj.axiom.application.features.users.getcurrentuserprofile.GetCurrentUserProfileQuery;
import com.studproj.axiom.application.features.users.getcurrentuserprofile.GetCurrentUserProfileQueryHandler;
import com.studproj.axiom.application.features.users.getuserbyid.GetUserByIdQuery;
import com.studproj.axiom.application.features.users.getuserbyid.GetUserByIdQueryHandler;
import com.studproj.axiom.application.features.users.updateuserprofile.UpdateUserProfileCommand;
import com.studproj.axiom.application.features.users.updateuserprofile.UpdateUserProfileCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private GetAllUsersQueryHandler getAllUsersHandler;
    private GetUserByIdQueryHandler getUserByIdHandler;
    private GetCurrentUserProfileQueryHandler getCurrentUserProfileHandler;
    private UpdateUserProfileCommandHandler updateUserProfileHandler;
    private UserController controller;

    @BeforeEach
    void setUp() {
        getAllUsersHandler = mock(GetAllUsersQueryHandler.class);
        getUserByIdHandler = mock(GetUserByIdQueryHandler.class);
        getCurrentUserProfileHandler = mock(GetCurrentUserProfileQueryHandler.class);
        updateUserProfileHandler = mock(UpdateUserProfileCommandHandler.class);
        controller = new UserController(
                getAllUsersHandler,
                getUserByIdHandler,
                getCurrentUserProfileHandler,
                updateUserProfileHandler
        );
    }

    @Test
    void getAllUsersDelegatesQuery() {
        List<UserDto> users = List.of(userDto(UUID.randomUUID()));
        when(getAllUsersHandler.handle(new GetAllUsersQuery())).thenReturn(users);

        var response = controller.getAllUsers();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(users);
    }

    @Test
    void getUserByIdReturnsOkOrNotFound() {
        UUID userId = UUID.randomUUID();
        UserDto user = userDto(userId);
        when(getUserByIdHandler.handle(new GetUserByIdQuery(userId))).thenReturn(Optional.of(user));

        var found = controller.getUserById(userId);
        assertThat(found.getStatusCode().value()).isEqualTo(200);
        assertThat(found.getBody()).isEqualTo(user);

        UUID missingUserId = UUID.randomUUID();
        when(getUserByIdHandler.handle(new GetUserByIdQuery(missingUserId))).thenReturn(Optional.empty());

        var missing = controller.getUserById(missingUserId);
        assertThat(missing.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getCurrentUserProfileDelegatesQuery() {
        UserDto user = userDto(UUID.randomUUID());
        when(getCurrentUserProfileHandler.handle(new GetCurrentUserProfileQuery())).thenReturn(user);

        var response = controller.getCurrentUserProfile();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(user);
    }

    @Test
    void updateCurrentUserProfileDelegatesCommand() {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                "Ada",
                "Lovelace",
                LocalDate.of(1990, 1, 1)
        );

        var response = controller.updateCurrentUserProfile(command);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(updateUserProfileHandler).handle(command);
    }

    private UserDto userDto(UUID id) {
        return new UserDto(id, "ada", "ada@example.com", "Ada", "Lovelace", LocalDate.of(1990, 1, 1));
    }
}
