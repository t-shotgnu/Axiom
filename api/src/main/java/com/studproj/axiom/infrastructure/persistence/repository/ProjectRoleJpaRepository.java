package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.infrastructure.persistence.entity.ProjectRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRoleJpaRepository extends JpaRepository<ProjectRoleEntity, UUID> {
    Optional<ProjectRoleEntity> findByCode(String code);
}