package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.WorkItemDto;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkItemQueryHandler {
    private final WorkItemRepository workItemRepository;

    public List<WorkItemDto> getWorkItemsByProject(UUID projectId) {
        return workItemRepository.findByProjectId(projectId).stream()
                .map(w -> new WorkItemDto(w.getId(), w.getControlNo(), w.getDescription(), w.getPriority(),
                        w.getType(), w.getStatus(), w.getDueDate(), w.getEstimatedEffort(),
                        w.getProjectId(), w.getAuthorId(), w.getAssigneeId()))
                .collect(Collectors.toList());
    }
}

