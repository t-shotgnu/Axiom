import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ProjectService, Project } from '../../core/services/project.service';
import { AuthService } from '../../core/services/auth.service';
import { WorkItemService, WorkItem, CreateWorkItemCommand } from '../../core/services/work-item.service';
import { ButtonComponent } from '../../shared/components/ui/button';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ButtonComponent,
  ],
  templateUrl: './tasks.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TasksComponent implements OnInit {
  protected readonly Math = Math;

  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);
  private readonly workItemService = inject(WorkItemService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);

  currentProject: Project | null = null;
  tasks: WorkItem[] = [];
  filteredTasks: WorkItem[] = [];
  visibleTasks: WorkItem[] = [];
  loading = false;

  // Filters
  searchText = '';
  onlyMyIssues = false;
  recentlyUpdated = false;

  // Pagination
  currentPage = 1;
  pageSize = 7;
  totalPages = 1;

  // Backwards-compatible API used by legacy tests
  fetchProjectId = '';
  projectId = '';
  newTask: Partial<CreateWorkItemCommand & { description?: string }> = {
    description: '',
    priority: 1,
    type: 'Task',
    status: 'New',
  };

  ngOnInit(): void {
    this.projectService.currentProject$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (project) => {
          this.currentProject = project;
          this.currentPage = 1;
          this.loadTasks();
        },
      });
  }

  constructor(workItemService?: WorkItemService) {
    if (workItemService) {
      (this as any).workItemService = workItemService as WorkItemService;
    }
  }

  createTask(): void {
    const pid = this.projectId || this.fetchProjectId || this.currentProject?.id;
    if (!pid) return;

    const payload: CreateWorkItemCommand = {
      description: (this.newTask.description || '').trim(),
      priority: (this.newTask.priority as number) || 1,
      type: (this.newTask.type as string) || 'Task',
      status: (this.newTask.status as string) || 'New',
      projectId: pid,
    };

    this.workItemService.createWorkItem(payload).subscribe({
      next: () => {
        this.newTask.description = '';
        this.fetchProjectId = pid;
        this.loadTasks();
      },
      error: (err) => console.error('Error creating task', err),
    });
  }

  loadTasks(): void {
    if (!this.currentProject) {
      this.tasks = [];
      this.filteredTasks = [];
      this.visibleTasks = [];
      this.totalPages = 1;
      this.cdr.markForCheck();
      return;
    }

    this.loading = true;
    this.cdr.markForCheck();

    this.workItemService.getWorkItems(this.currentProject.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.tasks = data || [];
          this.applyFiltersAndPagination();
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error fetching tasks', err);
          this.loading = false;
          this.cdr.markForCheck();
        },
      });
  }

  getCurrentUserEmail(): string {
    const token = this.authService.getToken();
    if (!token) return '';
    try {
      const payload = JSON.parse(atob(token.split('.')[1]!));
      return payload.sub || '';
    } catch {
      return '';
    }
  }

  isMyTask(task: WorkItem): boolean {
    if (!task.assigneeId) return false;
    const email = this.getCurrentUserEmail();
    if (!email) return false;
    const namePart = email.split('@')[0] || ''; // e.g. "john.doe"
    const normalizedName = namePart.replace('.', ' ').toLowerCase(); // "john doe"
    return task.assigneeId.toLowerCase().includes(normalizedName) || normalizedName.includes(task.assigneeId.toLowerCase());
  }

  applyFiltersAndPagination(): void {
    // 1. Search & Filter
    this.filteredTasks = this.tasks.filter((t) => {
      const matchesSearch = !this.searchText.trim() ||
        t.description.toLowerCase().includes(this.searchText.toLowerCase()) ||
        t.controlNo.toString().includes(this.searchText);

      const matchesMyIssues = !this.onlyMyIssues || this.isMyTask(t);

      return matchesSearch && matchesMyIssues;
    });

    // 2. Pagination
    this.totalPages = Math.ceil(this.filteredTasks.length / this.pageSize) || 1;
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }

    const startIdx = (this.currentPage - 1) * this.pageSize;
    const endIdx = startIdx + this.pageSize;
    this.visibleTasks = this.filteredTasks.slice(startIdx, endIdx);
  }

  onSearchChange(): void {
    this.currentPage = 1;
    this.applyFiltersAndPagination();
  }

  toggleOnlyMyIssues(): void {
    this.onlyMyIssues = !this.onlyMyIssues;
    this.currentPage = 1;
    this.applyFiltersAndPagination();
  }

  toggleRecentlyUpdated(): void {
    this.recentlyUpdated = !this.recentlyUpdated;
    // Mock effect or sorting by control number descending
    if (this.recentlyUpdated) {
      this.tasks.sort((a, b) => b.controlNo - a.controlNo);
    } else {
      this.tasks.sort((a, b) => a.controlNo - b.controlNo);
    }
    this.applyFiltersAndPagination();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.applyFiltersAndPagination();
  }

  getPagesArray(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  // Formatting helpers
  getPriorityLabel(priority: number): string {
    if (priority <= 2) return 'Highest';
    if (priority <= 4) return 'High';
    if (priority <= 6) return 'Medium';
    if (priority <= 8) return 'Low';
    return 'Lowest';
  }

  getPriorityIcon(priority: number): string {
    if (priority <= 2) return 'keyboard_double_arrow_up';
    if (priority <= 6) return 'keyboard_arrow_up';
    return 'keyboard_arrow_down';
  }

  getPriorityColorClass(priority: number): string {
    if (priority <= 2) return 'text-[#de350b]';
    if (priority <= 4) return 'text-[#ff5630]';
    if (priority <= 6) return 'text-[#0052cc]';
    if (priority <= 8) return 'text-[#42526e]';
    return 'text-[#8993a4]';
  }

  getStatusBadgeClass(status: string): string {
    const s = status.toLowerCase();
    if (s === 'resolved' || s === 'closed' || s === 'done') {
      return 'bg-green-500/10 text-green-600 border-green-500/20';
    }
    if (s === 'active' || s === 'in progress' || s === 'in_progress') {
      return 'bg-primary/10 text-primary border-primary/20';
    }
    if (s === 'review') {
      return 'bg-purple-500/10 text-purple-600 border-purple-500/20';
    }
    return 'bg-surface-container-high text-on-surface-variant border-outline-variant';
  }

  getStatusLabel(status: string): string {
    const s = status.toLowerCase();
    if (s === 'in_progress' || s === 'in progress') return 'IN PROGRESS';
    if (s === 'todo' || s === 'new') return 'TO DO';
    if (s === 'resolved') return 'REVIEW';
    if (s === 'closed') return 'DONE';
    return status.toUpperCase();
  }
}
