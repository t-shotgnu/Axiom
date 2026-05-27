package com.studproj.axiom.application.features.dashboard.getdashboardsummary;

import com.studproj.axiom.application.features.workitems.WorkItemDto;

import java.util.List;

public record DashboardSummaryDto(
        long activeProjectsCount,
        long openTasksCount,
        long resolvedTasksCount,
        List<WorkItemDto> recentTasks
) {}
