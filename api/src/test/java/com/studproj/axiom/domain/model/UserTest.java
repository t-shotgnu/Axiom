package com.studproj.axiom.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void activateMarksUserActiveAndTouchesModifiedOn() {
        User user = User.builder()
                .active(false)
                .build();

        user.activate();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getModifiedOn()).isNotNull();
    }

    @Test
    void activateAlreadyActiveUserStillUpdatesModifiedOn() {
        User user = User.builder()
                .active(true)
                .build();

        LocalDateTime before = user.getModifiedOn();

        user.activate();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getModifiedOn()).isNotNull();
        assertThat(user.getModifiedOn()).isNotEqualTo(before);
    }

    @Test
    void deactivateMarksUserInactiveAndTouchesModifiedOn() {
        User user = User.builder()
                .active(true)
                .build();

        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getModifiedOn()).isNotNull();
    }

    @Test
    void deactivateAlreadyInactiveUserStillUpdatesModifiedOn() {
        User user = User.builder()
                .active(false)
                .build();

        LocalDateTime before = user.getModifiedOn();

        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getModifiedOn()).isNotNull();
        assertThat(user.getModifiedOn()).isNotEqualTo(before);
    }

    @Test
    void updateLoginTimeTouchesLastLogin() {
        User user = User.builder().build();

        user.updateLoginTime();

        assertThat(user.getLastLogin()).isNotNull();
    }

    @Test
    void updateLoginTimeUpdatesExistingTimestamp() {
        LocalDateTime oldLogin = LocalDateTime.now().minusDays(1);

        User user = User.builder()
                .lastLogin(oldLogin)
                .build();

        user.updateLoginTime();

        assertThat(user.getLastLogin()).isAfter(oldLogin);
    }

    @Test
    void updateProfileSetsNamesAndDateOfBirthAndTouchesModifiedOn() {
        User user = User.builder()
                .firstName("A")
                .lastName("B")
                .build();

        LocalDate birthDate = LocalDate.of(1990, 5, 20);

        user.updateProfile("NewFirst", "NewLast", birthDate);

        assertThat(user.getFirstName()).isEqualTo("NewFirst");
        assertThat(user.getLastName()).isEqualTo("NewLast");
        assertThat(user.getDateOfBirth()).isEqualTo(birthDate);
        assertThat(user.getModifiedOn()).isNotNull();
    }

    @Test
    void updateProfileOverwritesExistingValues() {
        User user = User.builder()
                .firstName("Old")
                .lastName("User")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .build();

        LocalDate newBirthDate = LocalDate.of(2000, 10, 10);

        user.updateProfile("John", "Doe", newBirthDate);

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getDateOfBirth()).isEqualTo(newBirthDate);
    }

    @Test
    void updateProfileAllowsNullDateOfBirth() {
        User user = User.builder().build();

        user.updateProfile("John", "Doe", null);

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getDateOfBirth()).isNull();
        assertThat(user.getModifiedOn()).isNotNull();
    }

    @Test
    void changePasswordUpdatesPasswordAndTouchesModifiedOn() {
        User user = User.builder()
                .password("oldPassword")
                .build();

        user.changePassword("newPassword");

        assertThat(user.getPassword()).isEqualTo("newPassword");
        assertThat(user.getModifiedOn()).isNotNull();
    }

    @Test
    void changePasswordOverwritesExistingPassword() {
        User user = User.builder()
                .password("oldPassword")
                .build();

        user.changePassword("newPassword");

        assertThat(user.getPassword()).isNotEqualTo("oldPassword");
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }
}