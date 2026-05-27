package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaProjectRepository implements ProjectRepository {
    private final ProjectJpaRepository jpaRepository;

    @Override
    public void save(Project project) {
        jpaRepository.save(ProjectMapper.toEntity(project));
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return jpaRepository.findById(id).map(ProjectMapper::toDomain);
    }

    @Override
    public List<Project> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return jpaRepository.findByIdIn(ids).stream()
                .map(ProjectMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Project> findAll() {
        return jpaRepository.findAll().stream()
                .map(ProjectMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
