package com.studproj.axiom.application.dto.query;

import java.util.List;

public record DashboardSummaryDto(
        long activeProjectsCount,
        long openTasksCount,
        long resolvedTasksCount,
        List<WorkItemDto> recentTasks
) {}
