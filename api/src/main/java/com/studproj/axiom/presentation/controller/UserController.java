package com.studproj.axiom.presentation.controller;

import com.studproj.axiom.application.features.users.updateuserprofile.UpdateUserProfileCommand;
import com.studproj.axiom.application.features.users.updateuserprofile.UpdateUserProfileCommandHandler;
import com.studproj.axiom.application.features.users.getallusers.GetAllUsersQuery;
import com.studproj.axiom.application.features.users.getallusers.GetAllUsersQueryHandler;
import com.studproj.axiom.application.features.users.getuserbyid.GetUserByIdQuery;
import com.studproj.axiom.application.features.users.getuserbyid.GetUserByIdQueryHandler;
import com.studproj.axiom.application.features.users.getcurrentuserprofile.GetCurrentUserProfileQuery;
import com.studproj.axiom.application.features.users.getcurrentuserprofile.GetCurrentUserProfileQueryHandler;
import com.studproj.axiom.application.features.users.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final GetAllUsersQueryHandler getAllUsersQueryHandler;
    private final GetUserByIdQueryHandler getUserByIdQueryHandler;
    private final GetCurrentUserProfileQueryHandler getCurrentUserProfileQueryHandler;
    private final UpdateUserProfileCommandHandler updateUserProfileCommandHandler;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(getAllUsersQueryHandler.handle(new GetAllUsersQuery()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return getUserByIdQueryHandler.handle(new GetUserByIdQuery(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        return ResponseEntity.ok(getCurrentUserProfileQueryHandler.handle(new GetCurrentUserProfileQuery()));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUserProfile(@Valid @RequestBody UpdateUserProfileCommand command) {
        updateUserProfileCommandHandler.handle(command);
        return ResponseEntity.noContent().build();
    }
}
