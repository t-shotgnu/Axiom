import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DashboardService } from '../../core/services/dashboard.service';
import { WorkItem } from '../../core/services/work-item.service';
import { finalize } from 'rxjs';
import { TaskItemComponent } from '../../shared/components/task-item/task-item';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule, TaskItemComponent],
  templateUrl: './dashboard.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  loading = true;
  activeProjectsCount = 0;
  openTasksCount = 0;
  resolvedTasksCount = 0;
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
          this.openTasksCount = summary.openTasksCount;
          this.resolvedTasksCount = summary.resolvedTasksCount;
          this.recentTasks = summary.recentTasks ?? [];
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error fetching dashboard data', err);
          this.cdr.markForCheck();
        },
      });
  }
}
