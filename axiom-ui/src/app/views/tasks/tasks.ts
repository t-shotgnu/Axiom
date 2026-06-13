import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, HostListener, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, forkJoin } from 'rxjs';

import { ProjectService, Project } from '../../core/services/project.service';
import { WorkItemService, WorkItem, CreateWorkItemCommand, TaskRelationship } from '../../core/services/work-item.service';
import { ProjectMemberService } from '../../core/services/project-member.service';
import { UserService } from '../../core/services/user.service';
import { ToastService } from '../../core/services/toast.service';
import { extractErrorMessage } from '../../core/utils/error-utils';
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
  private readonly toastService = inject(ToastService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

  currentProject: Project | null = null;
  tasks: WorkItem[] = [];
  filteredTasks: any[] = [];
  visibleTasks: any[] = [];
  relationships: TaskRelationship[] = [];
  projectUsers: ProjectUser[] = [];
  currentUserId = '';
  loading = false;
  creating = false;
  showCreateDialog = false;
  showDepsDialog = false;
  selectedTaskForDeps: any = null;
  dependenciesList: any[] = [];

  /** IDs of parent items whose children are currently collapsed */
  collapsedItems = new Set<string>();

  // --- Inline creation (Jira-style) — bottom row creates Epics ---
  inlineDescription = '';
  inlineType = 'Epic';
  inlineCreateActive = false;
  showInlineTypePicker = false;
  inlineCreating = false;

  // --- Insert-after (hover + between rows) ---
  hoveredRowId: string | null = null;
  insertAfterTaskId: string | null = null;
  insertInlineDescription = '';
  insertInlineType = 'Task';
  insertInlineParentId: string | null = null;
  insertInlineParentType: string | null = null;
  showInsertTypePicker = false;
  insertInlineCreating = false;
  private hoverTimeout: any;

  /** Allowed child types per parent type — loaded once from the backend. */
  typeHierarchy: Record<string, string[]> = {};

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

    // Load hierarchy rules once (cached in service via shareReplay)
    this.workItemService.getTypeHierarchy()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (h) => { this.typeHierarchy = h; this.cdr.markForCheck(); },
        error: (err) => console.error('Failed to load type hierarchy', err),
      });

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

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.showInlineTypePicker) {
      this.showInlineTypePicker = false;
      this.cdr.markForCheck();
    }
    if (this.showInsertTypePicker) {
      this.showInsertTypePicker = false;
      this.cdr.markForCheck();
    }
  }

  onRowMouseEnter(taskId: string): void {
    clearTimeout(this.hoverTimeout);
    if (this.insertAfterTaskId !== null) return;
    this.hoveredRowId = taskId;
    this.cdr.markForCheck();
  }

  onRowMouseLeave(taskId: string): void {
    this.hoverTimeout = setTimeout(() => {
      if (this.hoveredRowId === taskId) {
        this.hoveredRowId = null;
        this.cdr.markForCheck();
      }
    }, 80);
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
      error: (err) => {
        this.toastService.error(extractErrorMessage(err, 'Failed to create issue.'));
        console.error('Error creating task', err);
      },
    });
  }

  /** Commits the inline create row — creates a task with the typed description and selected type. */
  commitInlineCreate(): void {
    const pid = this.currentProject?.id;
    const description = this.inlineDescription.trim();
    if (!pid || !description || this.inlineCreating) return;

    const payload: CreateWorkItemCommand = {
      description,
      priority: 5,
      type: this.inlineType,
      status: 'New',
      projectId: pid,
    };

    this.inlineCreating = true;
    this.workItemService.createWorkItem(payload).pipe(
      finalize(() => {
        this.inlineCreating = false;
        this.cdr.markForCheck();
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: () => {
        this.inlineDescription = '';
        this.inlineCreateActive = false;
        this.inlineType = 'Epic';
        this.loadTasks();
      },
      error: (err) => {
        this.toastService.error(extractErrorMessage(err, 'Failed to create issue.'));
        console.error('Error creating task inline', err);
      },
    });
  }

  cancelInlineCreate(): void {
    this.inlineDescription = '';
    this.inlineCreateActive = false;
    this.showInlineTypePicker = false;
    this.inlineType = 'Epic';
    this.cdr.markForCheck();
  }

  onInlineBlur(): void {
    // Delay so mousedown on confirm/cancel buttons fires first
    setTimeout(() => {
      if (!this.inlineDescription.trim()) {
        this.inlineCreateActive = false;
        this.cdr.markForCheck();
      }
    }, 150);
  }

  toggleInlineTypePicker(event: Event): void {
    event.stopPropagation();
    this.showInlineTypePicker = !this.showInlineTypePicker;
    this.cdr.markForCheck();
  }

  selectInlineType(type: string): void {
    this.inlineType = type;
    this.showInlineTypePicker = false;
    this.cdr.markForCheck();
  }

  // --- Insert-after methods ---

  openInsertAfter(event: Event, task: any): void {
    event.preventDefault();
    event.stopPropagation();
    this.insertAfterTaskId = task.id;
    this.insertInlineParentId = task.id;
    this.insertInlineParentType = task.type;
    this.insertInlineType = this.getDefaultChildType(task.type);
    this.insertInlineDescription = '';
    this.showInsertTypePicker = false;
    this.hoveredRowId = null;
    this.cdr.markForCheck();
    // Focus the input on next tick
    setTimeout(() => {
      const el = document.getElementById('insert-inline-input');
      el?.focus();
    }, 30);
  }

  cancelInsert(): void {
    this.insertAfterTaskId = null;
    this.insertInlineParentId = null;
    this.insertInlineParentType = null;
    this.insertInlineDescription = '';
    this.showInsertTypePicker = false;
    this.cdr.markForCheck();
  }

  commitInsert(): void {
    const pid = this.currentProject?.id;
    const description = this.insertInlineDescription.trim();
    if (!pid || !description || this.insertInlineCreating) return;

    const payload: CreateWorkItemCommand = {
      description,
      priority: 5,
      type: this.insertInlineType,
      status: 'New',
      projectId: pid,
    };

    this.insertInlineCreating = true;
    this.cdr.markForCheck();

    this.workItemService.createWorkItem(payload).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (newId) => {
        const finish = () => {
          this.insertInlineCreating = false;
          this.cancelInsert();
          this.loadTasks();
        };
        if (this.insertInlineParentId) {
          this.workItemService.createRelationship({
            sourceId: newId,
            targetId: this.insertInlineParentId,
            linkType: 'ChildOf',
          }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: finish,
            error: (err) => {
              this.toastService.error(extractErrorMessage(err, 'Issue created but parent link failed.'));
              console.error('Parent link failed', err);
              finish();
            },
          });
        } else {
          finish();
        }
      },
      error: (err) => {
        this.insertInlineCreating = false;
        this.toastService.error(extractErrorMessage(err, 'Failed to create issue.'));
        console.error('Error creating task (insert)', err);
        this.cdr.markForCheck();
      },
    });
  }

  onInsertBlur(event?: FocusEvent): void {
    const relatedTarget = event?.relatedTarget as HTMLElement;
    const container = document.getElementById('insert-inline-container');
    if (container && relatedTarget && container.contains(relatedTarget)) {
      return;
    }
    setTimeout(() => {
      const activeEl = document.activeElement;
      if (container && activeEl && container.contains(activeEl)) {
        return;
      }
      if (!this.insertInlineDescription.trim()) {
        this.cancelInsert();
      }
    }, 150);
  }

  toggleInsertTypePicker(event: Event): void {
    event.stopPropagation();
    this.showInsertTypePicker = !this.showInsertTypePicker;
    this.cdr.markForCheck();
  }
  selectInsertType(type: string): void {
    this.insertInlineType = type;
    this.showInsertTypePicker = false;
    this.cdr.markForCheck();
  }

  /** Returns valid child types for a given parent type (from backend-sourced hierarchy). */
  getChildTypeOptions(parentType: string | null): { value: string; label: string }[] {
    if (!parentType) return this.typeOptions;
    const allowed = this.typeHierarchy[parentType] ?? [];
    return this.typeOptions.filter(opt => allowed.includes(opt.value));
  }

  /** Returns the best default child type for a given parent type. */
  getDefaultChildType(parentType: string): string {
    const allowed = this.typeHierarchy[parentType] ?? [];
    // Prefer natural defaults; fall back to first in allowed list
    const defaults: Record<string, string> = {
      Epic: 'Feature',
      Feature: 'UserStory',
      UserStory: 'Task',
      Task: 'Subtask',
      Bug: 'Subtask',
      Subtask: 'Subtask',
    };
    const preferred = defaults[parentType];
    if (preferred && allowed.includes(preferred)) return preferred;
    return allowed[0] ?? 'Task';
  }

  loadTasks(): void {
    if (!this.currentProject) {
      this.tasks = [];
      this.relationships = [];
      this.filteredTasks = [];
      this.visibleTasks = [];
      this.totalPages = 1;
      this.cdr.markForCheck();
      return;
    }

    this.loading = true;
    this.cdr.markForCheck();

    forkJoin({
      tasks: this.workItemService.getWorkItems(this.currentProject.id),
      relationships: this.workItemService.getRelationshipsByProject(this.currentProject.id)
    }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: (res) => {
        this.tasks = res.tasks || [];
        this.relationships = res.relationships || [];
        this.applyFiltersAndPagination();
      },
      error: (err) => {
        console.error('Error fetching tasks and relationships', err);
      },
    });
  }

  isMyTask(task: WorkItem): boolean {
    return !!task.assigneeId && task.assigneeId === this.currentUserId;
  }

  applyFiltersAndPagination(): void {
    const parentMap = new Map<string, string>(); // childId -> parentId
    const childrenMap = new Map<string, string[]>(); // parentId -> childIds[]

    this.relationships.forEach(rel => {
      if (rel.linkType === 'ChildOf') {
        parentMap.set(rel.sourceId, rel.targetId);
        if (!childrenMap.has(rel.targetId)) {
          childrenMap.set(rel.targetId, []);
        }
        childrenMap.get(rel.targetId)!.push(rel.sourceId);
      }
    });

    const roots = this.tasks.filter(t => !parentMap.has(t.id) || !this.tasks.some(pt => pt.id === parentMap.get(t.id)));

    const ordered: any[] = [];
    const visited = new Set<string>();

    const traverse = (taskId: string, currentLevel: number, parentId: string | null) => {
      if (visited.has(taskId)) return;
      visited.add(taskId);
      const task = this.tasks.find(t => t.id === taskId);
      if (task) {
        const hasDependencies = this.relationships.some(r => r.sourceId === taskId && r.linkType === 'BlockedBy');
        const childIds = childrenMap.get(taskId) || [];
        const hasChildren = childIds.length > 0;
        ordered.push({
          ...task,
          level: currentLevel,
          hasDependencies,
          hasChildren,
          parentId: parentId ?? null,
        });
        const childTasks = this.tasks
          .filter(t => childIds.includes(t.id))
          .sort((a, b) => a.controlNo - b.controlNo);

        childTasks.forEach(ct => traverse(ct.id, currentLevel + 1, taskId));
      }
    };

    // Sort roots by controlNo depending on recentlyUpdated
    const sortedRoots = [...roots].sort((a, b) => {
      if (this.recentlyUpdated) {
        return b.controlNo - a.controlNo;
      } else {
        return a.controlNo - b.controlNo;
      }
    });

    sortedRoots.forEach(r => traverse(r.id, 0, null));

    // Also add any orphans that might not have been traversed
    this.tasks.forEach(t => {
      if (!visited.has(t.id)) {
        const hasDependencies = this.relationships.some(r => r.sourceId === t.id && r.linkType === 'BlockedBy');
        const childIds = childrenMap.get(t.id) || [];
        ordered.push({
          ...t,
          level: 0,
          hasDependencies,
          hasChildren: childIds.length > 0,
          parentId: null,
        });
      }
    });

    // 1. Search & Filter
    this.filteredTasks = ordered.filter((t) => {
      const matchesSearch = !this.searchText.trim() ||
        t.description.toLowerCase().includes(this.searchText.toLowerCase()) ||
        t.controlNo.toString().includes(this.searchText);

      const matchesMyIssues = !this.onlyMyIssues || this.isMyTask(t);

      return matchesSearch && matchesMyIssues;
    });

    // 2. Collapse: hide children whose ancestor is collapsed
    const visibleFiltered = this.filteredTasks.filter(t => {
      // Walk up parents – if any ancestor is collapsed, hide this task
      let parentId = t.parentId as string | undefined;
      while (parentId) {
        if (this.collapsedItems.has(parentId)) return false;
        const parentTask = this.filteredTasks.find((x: any) => x.id === parentId);
        parentId = parentTask?.parentId;
      }
      return true;
    });

    // 3. Pagination
    this.totalPages = Math.ceil(visibleFiltered.length / this.pageSize) || 1;
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }

    const startIdx = (this.currentPage - 1) * this.pageSize;
    const endIdx = startIdx + this.pageSize;
    this.visibleTasks = visibleFiltered.slice(startIdx, endIdx);
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
    this.applyFiltersAndPagination();
  }

  openDepsDialog(event: Event, task: any): void {
    event.stopPropagation();
    this.selectedTaskForDeps = task;
    const blockerIds = this.relationships
      .filter(r => r.sourceId === task.id && r.linkType === 'BlockedBy')
      .map(r => r.targetId);
    this.dependenciesList = this.tasks.filter(t => blockerIds.includes(t.id));
    this.showDepsDialog = true;
    this.cdr.markForCheck();
  }

  closeDepsDialog(): void {
    this.showDepsDialog = false;
    this.selectedTaskForDeps = null;
    this.dependenciesList = [];
    this.cdr.markForCheck();
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

  /** Returns the Material Symbols icon name for a given work item type. */
  getTypeIcon(type: string): string {
    switch (type) {
      case 'Epic':      return 'crown';
      case 'Feature':   return 'star';
      case 'UserStory': return 'menu_book';
      case 'Task':      return 'check_box';
      case 'Bug':       return 'bug_report';
      case 'Subtask':   return 'task_alt';
      default:          return 'assignment';
    }
  }

  /** Returns a Tailwind text-color class for a given work item type icon. */
  getTypeColor(type: string): string {
    switch (type) {
      case 'Epic':      return 'text-orange-500';
      case 'Feature':   return 'text-violet-500';
      case 'UserStory': return 'text-green-500';
      case 'Task':      return 'text-blue-500';
      case 'Bug':       return 'text-red-500';
      case 'Subtask':   return 'text-sky-400';
      default:          return 'text-on-surface-variant';
    }
  }

  /** Toggles the collapsed state of a parent item's children. */
  toggleCollapse(event: Event, taskId: string): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.collapsedItems.has(taskId)) {
      this.collapsedItems.delete(taskId);
    } else {
      this.collapsedItems.add(taskId);
    }
    this.applyFiltersAndPagination();
  }
}
