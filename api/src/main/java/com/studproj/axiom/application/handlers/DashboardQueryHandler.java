package com.studproj.axiom.application.handlers;

import com.studproj.axiom.application.dto.query.DashboardSummaryDto;
import com.studproj.axiom.application.dto.query.WorkItemDto;
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryHandler {
    private final ProjectRepository projectRepository;
    private final WorkItemRepository workItemRepository;

    public DashboardSummaryDto getSummary() {
        long activeProjects = projectRepository.countAll();
        long openTasks = workItemRepository.countByStatusIn(
                List.of(WorkItemStatus.New, WorkItemStatus.Active));
        long resolvedTasks = workItemRepository.countByStatusIn(
                List.of(WorkItemStatus.Resolved, WorkItemStatus.Closed));
        List<WorkItemDto> recentTasks = workItemRepository.findRecent(5).stream()
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
                        workItem.getAssigneeId(),
                        workItem.getNotes()))
                .toList();
        return new DashboardSummaryDto(activeProjects, openTasks, resolvedTasks, recentTasks);
    }
}
