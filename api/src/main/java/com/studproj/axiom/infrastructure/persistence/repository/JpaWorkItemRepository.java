package com.studproj.axiom.infrastructure.persistence.repository;

import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.infrastructure.persistence.mapper.WorkItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaWorkItemRepository implements WorkItemRepository {
    private final WorkItemJpaRepository jpaRepository;

    @Override
    public void save(WorkItem workItem) {
        jpaRepository.save(WorkItemMapper.toEntity(workItem));
    }

    @Override
    public Optional<WorkItem> findById(UUID id) {
        return jpaRepository.findById(id).map(WorkItemMapper::toDomain);
    }

    @Override
    public List<WorkItem> findByProjectId(UUID projectId) {
        return jpaRepository.findByProjectId(projectId).stream()
                .map(WorkItemMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
