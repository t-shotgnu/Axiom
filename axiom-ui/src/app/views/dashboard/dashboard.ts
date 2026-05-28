import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  DashboardAssigneeWorkload,
  DashboardPriorityBreakdown,
  DashboardProjectProgress,
  DashboardService,
  DashboardStatusBreakdown,
  DashboardTypeBreakdown,
} from '../../core/services/dashboard.service';
import { WorkItem } from '../../core/services/work-item.service';
import { finalize } from 'rxjs';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
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

  constructor(
    private readonly dashboardService: DashboardService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.fetchDashboardData();
  }

  fetchDashboardData() {
    this.dashboardService
      .getSummary()
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
