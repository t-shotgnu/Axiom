package com.studproj.axiom.application.features.users.updateuserprofile;

import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record UpdateUserProfileCommand(
    String firstName,
    String lastName,
    @Past LocalDate dateOfBirth
) {}
