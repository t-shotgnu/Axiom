package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.ProjectRole;
import com.studproj.axiom.domain.model.ProjectRoleType;
import com.studproj.axiom.domain.repository.ProjectRoleRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.ProjectRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaProjectRoleRepository implements ProjectRoleRepository {
    private final ProjectRoleJpaRepository jpaRepository;

    @Override
    public void save(ProjectRole role) {
        jpaRepository.save(ProjectRoleMapper.toEntity(role));
    }

    @Override
    public void saveAll(List<ProjectRole> roles) {
        jpaRepository.saveAll(roles.stream().map(ProjectRoleMapper::toEntity).toList());
    }

    @Override
    public Optional<ProjectRole> findByType(ProjectRoleType type) {
        return jpaRepository.findByCode(type.name()).map(ProjectRoleMapper::toDomain);
    }

    @Override
    public Optional<ProjectRole> findById(UUID id) {
        return jpaRepository.findById(id).map(ProjectRoleMapper::toDomain);
    }

    @Override
    public List<ProjectRole> findAll() {
        return jpaRepository.findAll().stream().map(ProjectRoleMapper::toDomain).collect(Collectors.toList());
    }
}