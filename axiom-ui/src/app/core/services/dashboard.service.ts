import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WorkItem } from './work-item.service';

export interface DashboardSummary {
  activeProjectsCount: number;
  totalTasksCount: number;
  openTasksCount: number;
  inProgressTasksCount: number;
  resolvedTasksCount: number;
  unassignedTasksCount: number;
  overdueTasksCount: number;
  completionPercent: number;
  statusBreakdown: DashboardStatusBreakdown[];
  typeBreakdown: DashboardTypeBreakdown[];
  priorityBreakdown: DashboardPriorityBreakdown[];
  projectProgress: DashboardProjectProgress[];
  assigneeWorkload: DashboardAssigneeWorkload[];
  recentTasks: WorkItem[];
}

export interface DashboardStatusBreakdown {
  status: string;
  label: string;
  count: number;
  percent: number;
}

export interface DashboardTypeBreakdown {
  type: string;
  label: string;
  count: number;
  percent: number;
}

export interface DashboardPriorityBreakdown {
  label: string;
  count: number;
  percent: number;
}

export interface DashboardProjectProgress {
  projectId: string;
  projectName: string;
  projectCode: string;
  totalTasks: number;
  openTasks: number;
  completedTasks: number;
  unassignedTasks: number;
  completionPercent: number;
}

export interface DashboardAssigneeWorkload {
  userId: string;
  displayName: string;
  assignedTasks: number;
  openTasks: number;
  completedTasks: number;
}

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly apiUrl = '/api/dashboard';

  constructor(private readonly http: HttpClient) {}

  getSummary(projectId?: string): Observable<DashboardSummary> {
    const url = projectId ? `${this.apiUrl}/summary?projectId=${projectId}` : `${this.apiUrl}/summary`;
    return this.http.get<DashboardSummary>(url);
  }
}
