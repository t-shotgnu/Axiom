package com.studproj.axiom.domain.model;

import org.junit.jupiter.api.Test;

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
    void deactivateMarksUserInactiveAndTouchesModifiedOn() {
        User user = User.builder()
                .active(true)
                .build();

        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getModifiedOn()).isNotNull();
    }

    @Test
    void updateLoginTimeTouchesLastLogin() {
        User user = User.builder().build();

        user.updateLoginTime();

        assertThat(user.getLastLogin()).isNotNull();
    }
}
