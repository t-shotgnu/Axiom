package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.auth.changepassword.ChangePasswordCommand;
import com.studproj.axiom.application.features.auth.changepassword.ChangePasswordCommandHandler;
import com.studproj.axiom.application.features.auth.login.LoginCommand;
import com.studproj.axiom.application.features.auth.login.LoginCommandHandler;
import com.studproj.axiom.application.features.auth.login.LoginResponse;
import com.studproj.axiom.application.features.auth.refreshtoken.RefreshTokenCommand;
import com.studproj.axiom.application.features.auth.refreshtoken.RefreshTokenCommandHandler;
import com.studproj.axiom.application.features.auth.refreshtoken.RefreshTokenResponse;
import com.studproj.axiom.application.features.auth.registeruser.RegisterUserCommand;
import com.studproj.axiom.application.features.auth.registeruser.RegisterUserCommandHandler;
import com.studproj.axiom.application.features.auth.registeruser.RegisterUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private RegisterUserCommandHandler registerHandler;
    private LoginCommandHandler loginHandler;
    private ChangePasswordCommandHandler changePasswordHandler;
    private RefreshTokenCommandHandler refreshTokenHandler;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        registerHandler = mock(RegisterUserCommandHandler.class);
        loginHandler = mock(LoginCommandHandler.class);
        changePasswordHandler = mock(ChangePasswordCommandHandler.class);
        refreshTokenHandler = mock(RefreshTokenCommandHandler.class);
        controller = new AuthController(registerHandler, loginHandler, changePasswordHandler, refreshTokenHandler);
    }

    @Test
    void registerReturnsHandlerResponse() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ada",
                "ada@example.com",
                "secret1",
                "Ada",
                "Lovelace",
                LocalDate.of(1990, 1, 1)
        );
        RegisterUserResponse handlerResponse = new RegisterUserResponse("jwt", "refresh");
        when(registerHandler.handle(command)).thenReturn(handlerResponse);

        var response = controller.register(command);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(handlerResponse);
    }

    @Test
    void loginReturnsHandlerResponse() {
        LoginCommand command = new LoginCommand("ada@example.com", "secret1");
        LoginResponse handlerResponse = new LoginResponse("jwt", "refresh");
        when(loginHandler.handle(command)).thenReturn(handlerResponse);

        var response = controller.login(command);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(handlerResponse);
    }

    @Test
    void changePasswordDelegatesAndReturnsOk() {
        ChangePasswordCommand command = new ChangePasswordCommand("old", "new-secret", "new-secret");

        var response = controller.changePassword(command);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(changePasswordHandler).handle(command);
    }

    @Test
    void refreshTokenReturnsHandlerResponse() {
        RefreshTokenCommand command = new RefreshTokenCommand("refresh");
        RefreshTokenResponse handlerResponse = new RefreshTokenResponse("next-jwt", "next-refresh");
        when(refreshTokenHandler.handle(command)).thenReturn(handlerResponse);

        var response = controller.refreshToken(command);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(handlerResponse);
    }
}
