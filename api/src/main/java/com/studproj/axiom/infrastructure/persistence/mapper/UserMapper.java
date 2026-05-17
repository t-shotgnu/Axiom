package com.studproj.axiom.infrastructure.persistence.mapper;

import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.infrastructure.persistence.entity.UserEntity;

public class UserMapper {
    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .userName(entity.getUserName())
                .emailAddress(entity.getEmailAddress())
                .password(entity.getPassword())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .createdOn(entity.getCreatedOn())
                .modifiedOn(entity.getModifiedOn())
                .active(entity.isActive())
                .lastLogin(entity.getLastLogin())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) return null;
        return UserEntity.builder()
                .id(domain.getId())
                .userName(domain.getUserName())
                .emailAddress(domain.getEmailAddress())
                .password(domain.getPassword())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .dateOfBirth(domain.getDateOfBirth())
                .createdOn(domain.getCreatedOn())
                .modifiedOn(domain.getModifiedOn())
                .active(domain.isActive())
                .lastLogin(domain.getLastLogin())
                .build();
    }
}
