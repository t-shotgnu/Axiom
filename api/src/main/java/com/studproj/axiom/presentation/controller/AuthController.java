package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.dto.command.LoginCommand;
import com.studproj.axiom.application.dto.command.RegisterUserCommand;
import com.studproj.axiom.application.dto.response.AuthResponse;
import com.studproj.axiom.application.handlers.AuthCommandHandler;
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
    private final AuthCommandHandler handler;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserCommand command) {
        var resp = handler.register(command);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginCommand command) {
        var resp = handler.login(command);
        return ResponseEntity.ok(resp);
    }
}
