package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.infrastructure.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
	List<ProjectEntity> findByIdIn(Collection<UUID> ids);
}
