import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ProjectService } from '../../core/services/project.service';
import { WorkItemService, WorkItem } from '../../core/services/work-item.service';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { TaskItemComponent } from '../../shared/components/task-item/task-item';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule, TaskItemComponent],
  templateUrl: './dashboard.html',
  styles: []
})
export class DashboardComponent implements OnInit {
  loading = true;
  activeProjectsCount = 0;
  openTasksCount = 0;
  resolvedTasksCount = 0;
  recentTasks: WorkItem[] = [];

  constructor(
    private projectService: ProjectService,
    private workItemService: WorkItemService
  ) {}

  ngOnInit() {
    this.fetchDashboardData();
  }

  fetchDashboardData() {
    this.projectService.getAllProjects().pipe(
      switchMap(projects => {
        this.activeProjectsCount = projects.length;
        if (projects.length === 0) {
          return of([]);
        }
        const tasksRequests = projects.map(p => 
          this.workItemService.getWorkItems(p.id).pipe(catchError(() => of([])))
        );
        return forkJoin(tasksRequests);
      }),
      map(tasksArrays => tasksArrays.flat())
    ).subscribe({
      next: (allTasks) => {
        this.openTasksCount = allTasks.filter(t => t.status === 'New' || t.status === 'Active').length;
        this.resolvedTasksCount = allTasks.filter(t => t.status === 'Resolved' || t.status === 'Closed').length;
        
        // Mock recent tasks by just taking the last 5 tasks in the array
        this.recentTasks = allTasks.slice(-5).reverse();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching dashboard data', err);
        this.loading = false;
      }
    });
  }
}
