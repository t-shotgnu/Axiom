package com.studproj.axiom.infrastructure.query;

import com.studproj.axiom.application.dto.query.WorkItemDto;
import com.studproj.axiom.infrastructure.persistence.repository.WorkItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkItemQueryHandler {
    private final WorkItemJpaRepository workItemJpaRepository;

    public List<WorkItemDto> getWorkItemsByProject(UUID projectId) {
        return workItemJpaRepository.findByProjectId(projectId).stream()
                .map(workItem -> new WorkItemDto(
                        workItem.getId(),
                        workItem.getControlNo(),
                        workItem.getDescription(),
                        workItem.getPriority(),
                        workItem.getType(),
                        workItem.getStatus(),
                        workItem.getDueDate(),
                        workItem.getEstimatedEffort(),
                        workItem.getProjectId(),
                        workItem.getAuthorId(),
                        workItem.getAssigneeId()))
                .toList();
    }

    public Optional<WorkItemDto> getWorkItemById(UUID id) {
        return workItemJpaRepository.findById(id)
                .map(workItem -> new WorkItemDto(
                        workItem.getId(),
                        workItem.getControlNo(),
                        workItem.getDescription(),
                        workItem.getPriority(),
                        workItem.getType(),
                        workItem.getStatus(),
                        workItem.getDueDate(),
                        workItem.getEstimatedEffort(),
                        workItem.getProjectId(),
                        workItem.getAuthorId(),
                        workItem.getAssigneeId()));
    }
}
