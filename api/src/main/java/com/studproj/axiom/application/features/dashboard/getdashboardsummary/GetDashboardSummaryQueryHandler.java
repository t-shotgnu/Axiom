package com.studproj.axiom.application.features.dashboard.getdashboardsummary;

import com.studproj.axiom.application.features.workitems.WorkItemDto;
import com.studproj.axiom.domain.model.Project;
import com.studproj.axiom.domain.model.ProjectMembership;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.model.WorkItem;
import com.studproj.axiom.domain.model.WorkItemStatus;
import com.studproj.axiom.domain.model.WorkItemType;
import com.studproj.axiom.domain.repository.ProjectMembershipRepository;
import com.studproj.axiom.domain.repository.ProjectRepository;
import com.studproj.axiom.domain.repository.UserRepository;
import com.studproj.axiom.domain.repository.WorkItemRepository;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDashboardSummaryQueryHandler {
    private final ProjectRepository projectRepository;
    private final WorkItemRepository workItemRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DashboardSummaryDto handle(GetDashboardSummaryQuery query) {
        UUID userId = authenticatedUserProvider.getAuthenticatedUserId();
        List<UUID> userProjectIds = projectMembershipRepository.findByUserId(userId).stream()
                .map(ProjectMembership::getProjectId)
                .toList();

        List<Project> projects = projectRepository.findByIds(userProjectIds);
        if (query.projectId() != null) {
            projects = projects.stream()
                    .filter(project -> project.getId().equals(query.projectId()))
                    .toList();
        }

        Map<UUID, List<WorkItem>> tasksByProject = projects.stream()
                .collect(Collectors.toMap(Project::getId, project -> workItemRepository.findByProjectId(project.getId())));
        List<WorkItem> allTasks = tasksByProject.values().stream()
                .flatMap(List::stream)
                .toList();

        long activeProjects = projects.size();
        long totalTasks = allTasks.size();
        long openTasks = allTasks.stream().filter(this::isOpen).count();
        long inProgressTasks = allTasks.stream().filter(this::isInProgress).count();
        long resolvedTasks = allTasks.stream().filter(this::isCompleted).count();
        long unassignedTasks = allTasks.stream().filter(workItem -> workItem.getAssigneeId() == null).count();
        long overdueTasks = allTasks.stream().filter(this::isOverdue).count();
        int completionPercent = percent(resolvedTasks, totalTasks);

        List<DashboardSummaryDto.StatusBreakdownDto> statusBreakdown = Arrays.stream(WorkItemStatus.values())
                .map(status -> {
                    long count = allTasks.stream().filter(workItem -> workItem.getStatus() == status).count();
                    return new DashboardSummaryDto.StatusBreakdownDto(
                            status.name(),
                            statusLabel(status),
                            count,
                            percent(count, totalTasks));
                })
                .toList();

        List<DashboardSummaryDto.TypeBreakdownDto> typeBreakdown = Arrays.stream(WorkItemType.values())
                .map(type -> {
                    long count = allTasks.stream().filter(workItem -> workItem.getType() == type).count();
                    return new DashboardSummaryDto.TypeBreakdownDto(
                            type.name(),
                            typeLabel(type),
                            count,
                            percent(count, totalTasks));
                })
                .toList();

        List<DashboardSummaryDto.PriorityBreakdownDto> priorityBreakdown = buildPriorityBreakdown(allTasks);
        List<DashboardSummaryDto.ProjectProgressDto> projectProgress = buildProjectProgress(projects, tasksByProject);
        List<DashboardSummaryDto.AssigneeWorkloadDto> assigneeWorkload = buildAssigneeWorkload(allTasks);

        List<WorkItemDto> recentTasks = allTasks.stream()
                .sorted(Comparator.comparing(WorkItem::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(this::toDto)
                .toList();

        return new DashboardSummaryDto(
                activeProjects,
                totalTasks,
                openTasks,
                inProgressTasks,
                resolvedTasks,
                unassignedTasks,
                overdueTasks,
                completionPercent,
                statusBreakdown,
                typeBreakdown,
                priorityBreakdown,
                projectProgress,
                assigneeWorkload,
                recentTasks);
    }

    private List<DashboardSummaryDto.PriorityBreakdownDto> buildPriorityBreakdown(List<WorkItem> allTasks) {
        long totalTasks = allTasks.size();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("High", allTasks.stream().filter(workItem -> workItem.getPriority() != null && workItem.getPriority() <= 3).count());
        counts.put("Medium", allTasks.stream().filter(workItem -> workItem.getPriority() != null && workItem.getPriority() > 3 && workItem.getPriority() <= 6).count());
        counts.put("Low", allTasks.stream().filter(workItem -> workItem.getPriority() == null || workItem.getPriority() > 6).count());

        return counts.entrySet().stream()
                .map(entry -> new DashboardSummaryDto.PriorityBreakdownDto(entry.getKey(), entry.getValue(), percent(entry.getValue(), totalTasks)))
                .toList();
    }

    private List<DashboardSummaryDto.ProjectProgressDto> buildProjectProgress(List<Project> projects, Map<UUID, List<WorkItem>> tasksByProject) {
        return projects.stream()
                .map(project -> {
                    List<WorkItem> tasks = tasksByProject.getOrDefault(project.getId(), List.of());
                    long totalTasks = tasks.size();
                    long completedTasks = tasks.stream().filter(this::isCompleted).count();
                    long openTasks = tasks.stream().filter(this::isOpen).count();
                    long unassignedTasks = tasks.stream().filter(workItem -> workItem.getAssigneeId() == null).count();

                    return new DashboardSummaryDto.ProjectProgressDto(
                            project.getId(),
                            project.getName(),
                            project.getCode(),
                            totalTasks,
                            openTasks,
                            completedTasks,
                            unassignedTasks,
                            percent(completedTasks, totalTasks));
                })
                .sorted(Comparator
                        .comparingLong(DashboardSummaryDto.ProjectProgressDto::totalTasks)
                        .reversed()
                        .thenComparing(DashboardSummaryDto.ProjectProgressDto::projectName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(6)
                .toList();
    }

    private List<DashboardSummaryDto.AssigneeWorkloadDto> buildAssigneeWorkload(List<WorkItem> allTasks) {
        Map<UUID, List<WorkItem>> tasksByAssignee = allTasks.stream()
                .filter(workItem -> workItem.getAssigneeId() != null)
                .collect(Collectors.groupingBy(WorkItem::getAssigneeId));

        return tasksByAssignee.entrySet().stream()
                .map(entry -> {
                    List<WorkItem> tasks = entry.getValue();
                    long completedTasks = tasks.stream().filter(this::isCompleted).count();
                    long openTasks = tasks.stream().filter(this::isOpen).count();
                    return new DashboardSummaryDto.AssigneeWorkloadDto(
                            entry.getKey(),
                            displayName(entry.getKey()),
                            tasks.size(),
                            openTasks,
                            completedTasks);
                })
                .sorted(Comparator
                        .comparingLong(DashboardSummaryDto.AssigneeWorkloadDto::openTasks)
                        .reversed()
                        .thenComparing(DashboardSummaryDto.AssigneeWorkloadDto::displayName))
                .limit(6)
                .toList();
    }

    private boolean isOpen(WorkItem workItem) {
        return workItem.getStatus() == WorkItemStatus.New
                || workItem.getStatus() == WorkItemStatus.Active
                || workItem.getStatus() == WorkItemStatus.InDevelopment
                || workItem.getStatus() == WorkItemStatus.InTesting;
    }

    private boolean isInProgress(WorkItem workItem) {
        return workItem.getStatus() == WorkItemStatus.Active
                || workItem.getStatus() == WorkItemStatus.InDevelopment
                || workItem.getStatus() == WorkItemStatus.InTesting;
    }

    private boolean isCompleted(WorkItem workItem) {
        return workItem.getStatus() == WorkItemStatus.Resolved
                || workItem.getStatus() == WorkItemStatus.Closed;
    }

    private boolean isOverdue(WorkItem workItem) {
        return workItem.getDueDate() != null
                && workItem.getDueDate().isBefore(LocalDateTime.now())
                && !isCompleted(workItem);
    }

    private String displayName(UUID userId) {
        return userRepository.findById(userId)
                .map(this::displayName)
                .orElse(userId.toString());
    }

    private String displayName(User user) {
        String fullName = List.of(nullToBlank(user.getFirstName()), nullToBlank(user.getLastName())).stream()
                .filter(part -> !part.isBlank())
                .collect(Collectors.joining(" "));

        if (!fullName.isBlank()) {
            return fullName;
        }

        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            return user.getUserName();
        }

        return user.getEmailAddress();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private int percent(long value, long total) {
        if (total <= 0) {
            return 0;
        }

        return (int) Math.round((value * 100.0) / total);
    }

    private String statusLabel(WorkItemStatus status) {
        return switch (status) {
            case New -> "To Do";
            case Active -> "Active";
            case InDevelopment -> "In Development";
            case InTesting -> "In Testing";
            case Resolved -> "Resolved";
            case Closed -> "Closed";
        };
    }

    private String typeLabel(WorkItemType type) {
        return switch (type) {
            case Epic -> "Epic";
            case Feature -> "Feature";
            case UserStory -> "User Story";
            case Task -> "Task";
            case Bug -> "Bug";
            case Subtask -> "Subtask";
        };
    }

    private WorkItemDto toDto(WorkItem workItem) {
        return new WorkItemDto(
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
                workItem.getNotes());
    }
}
