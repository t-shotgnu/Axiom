import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  DashboardAssigneeWorkload,
  DashboardPriorityBreakdown,
  DashboardProjectProgress,
  DashboardService,
  DashboardStatusBreakdown,
  DashboardTypeBreakdown,
} from '../../core/services/dashboard.service';
import { ProjectService } from '../../core/services/project.service';
import { WorkItem } from '../../core/services/work-item.service';
import { Subscription, finalize } from 'rxjs';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit, OnDestroy {
  loading = true;
  activeProjectsCount = 0;
  totalTasksCount = 0;
  openTasksCount = 0;
  inProgressTasksCount = 0;
  resolvedTasksCount = 0;
  unassignedTasksCount = 0;
  overdueTasksCount = 0;
  completionPercent = 0;
  statusBreakdown: DashboardStatusBreakdown[] = [];
  typeBreakdown: DashboardTypeBreakdown[] = [];
  priorityBreakdown: DashboardPriorityBreakdown[] = [];
  projectProgress: DashboardProjectProgress[] = [];
  assigneeWorkload: DashboardAssigneeWorkload[] = [];
  recentTasks: WorkItem[] = [];

  private projectSub?: Subscription;

  constructor(
    private readonly dashboardService: DashboardService,
    private readonly projectService: ProjectService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.projectSub = this.projectService.currentProject$.subscribe({
      next: (project) => {
        this.loading = true;
        this.cdr.markForCheck();
        this.fetchDashboardData(project?.id);
      },
    });
  }

  ngOnDestroy() {
    this.projectSub?.unsubscribe();
  }

  fetchDashboardData(projectId?: string) {
    this.dashboardService
      .getSummary(projectId)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (summary) => {
          this.activeProjectsCount = summary.activeProjectsCount;
          this.totalTasksCount = summary.totalTasksCount || 0;
          this.openTasksCount = summary.openTasksCount;
          this.inProgressTasksCount = summary.inProgressTasksCount || 0;
          this.resolvedTasksCount = summary.resolvedTasksCount;
          this.unassignedTasksCount = summary.unassignedTasksCount || 0;
          this.overdueTasksCount = summary.overdueTasksCount || 0;
          this.completionPercent = summary.completionPercent || 0;
          this.statusBreakdown = summary.statusBreakdown ?? [];
          this.typeBreakdown = summary.typeBreakdown ?? [];
          this.priorityBreakdown = summary.priorityBreakdown ?? [];
          this.projectProgress = summary.projectProgress ?? [];
          this.assigneeWorkload = summary.assigneeWorkload ?? [];
          this.recentTasks = summary.recentTasks ?? [];
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error fetching dashboard data', err);
          this.cdr.markForCheck();
        },
      });
  }

  get hasIssues(): boolean {
    return this.totalTasksCount > 0;
  }

  get progressStyle(): string {
    return `width: ${this.clampPercent(this.completionPercent)}%`;
  }

  getStatusBadgeClass(status: string): string {
    const normalized = status.toLowerCase();
    if (normalized === 'resolved' || normalized === 'closed') {
      return 'bg-green-500/10 text-green-700 border-green-500/20';
    }

    if (normalized === 'active' || normalized === 'indevelopment' || normalized === 'intesting') {
      return 'bg-primary/10 text-primary border-primary/20';
    }

    return 'bg-surface-container-high text-on-surface-variant border-outline-variant';
  }

  getIssueIcon(type: string): string {
    if (type === 'Bug') return 'bug_report';
    if (type === 'UserStory') return 'menu_book';
    if (type === 'Epic') return 'bolt';
    if (type === 'Feature') return 'widgets';
    return 'assignment';
  }

  getIssueTypeColorClass(type: string): string {
    if (type === 'Bug') return 'text-red-600 bg-red-500/10 border-red-500/20';
    if (type === 'UserStory') return 'text-amber-600 bg-amber-500/10 border-amber-500/20';
    if (type === 'Epic') return 'text-purple-600 bg-purple-500/10 border-purple-500/20';
    if (type === 'Feature') return 'text-emerald-600 bg-emerald-500/10 border-emerald-500/20';
    return 'text-blue-600 bg-blue-500/10 border-blue-500/20';
  }

  getIssueTypeIconColor(type: string): string {
    if (type === 'Bug') return 'text-red-500';
    if (type === 'UserStory') return 'text-amber-500';
    if (type === 'Epic') return 'text-purple-600';
    if (type === 'Feature') return 'text-emerald-500';
    return 'text-blue-500';
  }

  getStatusLabel(status: string): string {
    const match = this.statusBreakdown.find((item) => item.status === status);
    if (match) {
      return match.label;
    }

    if (status === 'InDevelopment') return 'In Development';
    if (status === 'InTesting') return 'In Testing';
    if (status === 'New') return 'To Do';
    return status;
  }

  getProjectProgressStyle(project: DashboardProjectProgress): string {
    return `width: ${this.clampPercent(project.completionPercent)}%`;
  }

  getBreakdownStyle(item: { percent: number }): string {
    return `width: ${this.clampPercent(item.percent)}%`;
  }

  getBarHeightStyle(item: { percent: number; count: number }): string {
    const height = item.count > 0 ? Math.max(12, this.clampPercent(item.percent)) : 0;
    return `height: ${height}%`;
  }

  getAssigneeInitials(assignee: DashboardAssigneeWorkload): string {
    return assignee.displayName
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('') || '?';
  }

  private clampPercent(value: number): number {
    if (!Number.isFinite(value)) {
      return 0;
    }

    return Math.min(100, Math.max(0, value));
  }
}
