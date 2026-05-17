package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.dto.command.UpdateUserProfileCommand;
import com.studproj.axiom.application.dto.query.UserDto;
import com.studproj.axiom.application.handlers.UserCommandHandler;
import com.studproj.axiom.application.handlers.UserQueryHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserQueryHandler queryHandler;
    private final UserCommandHandler commandHandler;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(queryHandler.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return queryHandler.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        return ResponseEntity.ok(queryHandler.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUserProfile(@Valid @RequestBody UpdateUserProfileCommand command) {
        commandHandler.updateUserProfile(command);
        return ResponseEntity.noContent().build();
    }
}
