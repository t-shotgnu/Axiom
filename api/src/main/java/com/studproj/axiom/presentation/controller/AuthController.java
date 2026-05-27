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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUserCommandHandler registerUserCommandHandler;
    private final LoginCommandHandler loginCommandHandler;
    private final ChangePasswordCommandHandler changePasswordCommandHandler;
    private final RefreshTokenCommandHandler refreshTokenCommandHandler;

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserCommand command) {
        var resp = registerUserCommandHandler.handle(command);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginCommand command) {
        var resp = loginCommandHandler.handle(command);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordCommand command) {
        changePasswordCommandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenCommand command) {
        var resp = refreshTokenCommandHandler.handle(command);
        return ResponseEntity.ok(resp);
    }
}
