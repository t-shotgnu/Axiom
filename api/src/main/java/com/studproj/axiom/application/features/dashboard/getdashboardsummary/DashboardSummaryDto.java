package com.studproj.axiom.application.features.dashboard.getdashboardsummary;

import com.studproj.axiom.application.features.workitems.WorkItemDto;

import java.util.List;
import java.util.UUID;

public record DashboardSummaryDto(
        long activeProjectsCount,
        long totalTasksCount,
        long openTasksCount,
        long inProgressTasksCount,
        long resolvedTasksCount,
        long unassignedTasksCount,
        long overdueTasksCount,
        int completionPercent,
        List<StatusBreakdownDto> statusBreakdown,
        List<TypeBreakdownDto> typeBreakdown,
        List<PriorityBreakdownDto> priorityBreakdown,
        List<ProjectProgressDto> projectProgress,
        List<AssigneeWorkloadDto> assigneeWorkload,
        List<WorkItemDto> recentTasks
) {
    public record StatusBreakdownDto(String status, String label, long count, int percent) {}

    public record TypeBreakdownDto(String type, String label, long count, int percent) {}

    public record PriorityBreakdownDto(String label, long count, int percent) {}

    public record ProjectProgressDto(
            UUID projectId,
            String projectName,
            String projectCode,
            long totalTasks,
            long openTasks,
            long completedTasks,
            long unassignedTasks,
            int completionPercent
    ) {}

    public record AssigneeWorkloadDto(
            UUID userId,
            String displayName,
            long assignedTasks,
            long openTasks,
            long completedTasks
    ) {}
}
