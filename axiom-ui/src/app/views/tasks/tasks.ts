import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { ProjectService, Project } from '../../core/services/project.service';
import { WorkItemService, WorkItem, CreateWorkItemCommand } from '../../core/services/work-item.service';
import { ProjectMemberService } from '../../core/services/project-member.service';
import { UserService } from '../../core/services/user.service';
import { ButtonComponent } from '../../shared/components/ui/button';
import { DialogComponent } from '../../shared/components/ui/dialog';
import { InputComponent } from '../../shared/components/ui/input';
import { SelectComponent } from '../../shared/components/ui/select';

type ProjectUser = {
  id: string;
  userName?: string;
  fullName: string;
  email?: string;
  role: 'MEMBER' | 'ADMIN';
};

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ButtonComponent,
    DialogComponent,
    InputComponent,
    SelectComponent,
  ],
  templateUrl: './tasks.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TasksComponent implements OnInit {
  protected readonly Math = Math;

  protected readonly typeOptions = [
    { value: 'Task', label: 'Task' },
    { value: 'Bug', label: 'Bug' },
    { value: 'Feature', label: 'Feature' },
    { value: 'Epic', label: 'Epic' },
    { value: 'UserStory', label: 'User Story' },
    { value: 'Subtask', label: 'Subtask' }
  ];

  protected readonly statusOptions = [
    { value: 'New', label: 'New' },
    { value: 'Active', label: 'Active' },
    { value: 'InDevelopment', label: 'In Development' },
    { value: 'InTesting', label: 'In Testing' },
    { value: 'Resolved', label: 'Resolved' },
    { value: 'Closed', label: 'Closed' }
  ];

  get assigneeOptions() {
    const opts = [{ value: '', label: 'Unassigned' }];
    for (const u of this.projectUsers) {
      opts.push({ value: u.id, label: u.fullName });
    }
    return opts;
  }

  private readonly projectService = inject(ProjectService);
  private readonly workItemService = inject(WorkItemService);
  private readonly memberService = inject(ProjectMemberService);
  private readonly userService = inject(UserService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

  currentProject: Project | null = null;
  tasks: WorkItem[] = [];
  filteredTasks: WorkItem[] = [];
  visibleTasks: WorkItem[] = [];
  projectUsers: ProjectUser[] = [];
  currentUserId = '';
  loading = false;
  creating = false;
  showCreateDialog = false;

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
    this.loadCurrentUser();

    this.projectService.currentProject$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (project) => {
          this.currentProject = project;
          this.projectId = project?.id || '';
          this.currentPage = 1;
          this.loadProjectMembers(project?.id || '');
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

    const description = (this.newTask.description || '').trim();
    if (!description) return;

    const assigneeId = this.newTask.assigneeId?.trim();
    const payload: CreateWorkItemCommand = {
      description,
      priority: (this.newTask.priority as number) || 1,
      type: (this.newTask.type as string) || 'Task',
      status: (this.newTask.status as string) || 'New',
      projectId: pid,
      ...(assigneeId ? { assigneeId } : {}),
    };

    this.creating = true;
    this.workItemService.createWorkItem(payload).pipe(
      finalize(() => {
        this.creating = false;
        this.cdr.markForCheck();
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: () => {
        this.newTask = this.emptyTask();
        this.fetchProjectId = pid;
        this.showCreateDialog = false;
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

  isMyTask(task: WorkItem): boolean {
    return !!task.assigneeId && task.assigneeId === this.currentUserId;
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

  openCreateDialog(): void {
    if (!this.currentProject) return;
    this.newTask = this.emptyTask();
    this.showCreateDialog = true;
  }

  navigateToCreate(): void {
    this.router.navigate(['/tasks/new']);
  }

  cancelCreateDialog(): void {
    this.showCreateDialog = false;
    this.newTask = this.emptyTask();
  }

  private emptyTask(): Partial<CreateWorkItemCommand & { description?: string }> {
    return {
      description: '',
      priority: 1,
      type: 'Task',
      status: 'New',
      assigneeId: '',
    };
  }

  private loadCurrentUser(): void {
    this.userService.getCurrentUserProfile()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => {
          this.currentUserId = user.id;
          this.applyFiltersAndPagination();
          this.cdr.markForCheck();
        },
        error: (err) => console.error('Error loading current user profile', err),
      });
  }

  private loadProjectMembers(projectId: string): void {
    if (!projectId) {
      this.projectUsers = [];
      this.cdr.markForCheck();
      return;
    }

    this.memberService.getProjectMembers(projectId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (members) => {
          this.projectUsers = members.map((member) => ({
            id: member.userId,
            userName: member.userName,
            fullName: `${(member.firstName || '').trim()} ${(member.lastName || '').trim()}`.trim() || member.userName || member.emailAddress || 'Unknown',
            email: member.emailAddress,
            role: member.role,
          }));
          this.cdr.markForCheck();
        },
        error: (err) => console.error('Error loading project members', err),
      });
  }

  getAssigneeName(task: WorkItem): string {
    if (!task.assigneeId) {
      return 'Unassigned';
    }

    const member = this.projectUsers.find((user) => user.id === task.assigneeId);
    return member?.fullName || member?.userName || task.assigneeId;
  }

  getAssigneeInitials(task: WorkItem): string {
    const name = this.getAssigneeName(task);
    if (!task.assigneeId) {
      return '?';
    }

    return name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('') || '?';
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
    if (s === 'active' || s === 'in progress' || s === 'in_progress' || s === 'indevelopment' || s === 'intesting') {
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
    if (s === 'indevelopment') return 'IN DEVELOPMENT';
    if (s === 'intesting') return 'IN TESTING';
    if (s === 'todo' || s === 'new') return 'TO DO';
    if (s === 'resolved') return 'REVIEW';
    if (s === 'closed') return 'DONE';
    return status.toUpperCase();
  }
}
