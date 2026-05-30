package com.studproj.axiom.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String userName;
    private final String emailAddress;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private final LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private boolean active;
    private LocalDateTime lastLogin;

    public User(UUID id, String userName, String emailAddress, String password, String firstName, String lastName, 
                LocalDate dateOfBirth, LocalDateTime createdOn, LocalDateTime modifiedOn, boolean active, LocalDateTime lastLogin) {
        this.id = id;
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
        this.active = active;
        this.lastLogin = lastLogin;
    }

    public UUID getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void activate() {
        this.active = true;
        this.modifiedOn = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.modifiedOn = LocalDateTime.now();
    }

    public void updateLoginTime() {
        this.lastLogin = LocalDateTime.now();
    }

    public void updateProfile(String firstName, String lastName, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.modifiedOn = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.modifiedOn = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String userName;
        private String emailAddress;
        private String password;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private LocalDateTime createdOn;
        private LocalDateTime modifiedOn;
        private boolean active;
        private LocalDateTime lastLogin;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder createdOn(LocalDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder modifiedOn(LocalDateTime modifiedOn) {
            this.modifiedOn = modifiedOn;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder lastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public User build() {
            return new User(id, userName, emailAddress, password, firstName, lastName, dateOfBirth, createdOn, modifiedOn, active, lastLogin);
        }
    }
}
