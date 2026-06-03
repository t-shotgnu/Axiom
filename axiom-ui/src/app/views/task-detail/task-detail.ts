import { Component, OnInit, inject, ChangeDetectionStrategy, ChangeDetectorRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { WorkItemService, WorkItem, CreateWorkItemCommand } from '../../core/services/work-item.service';
import { ProjectService, Project } from '../../core/services/project.service';
import { Router } from '@angular/router';
import { ProjectMemberService } from '../../core/services/project-member.service';
import { UserService, User } from '../../core/services/user.service';
import { CommentService, Comment } from '../../core/services/comment.service';
import { AttachmentService, Attachment } from '../../core/services/attachment.service';
import { ButtonComponent } from '../../shared/components/ui/button';
import { DialogComponent } from '../../shared/components/ui/dialog';
import { SelectComponent } from '../../shared/components/ui/select';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ButtonComponent, DialogComponent, SelectComponent],
  templateUrl: './task-detail.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TaskDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);
  private readonly workItemService = inject(WorkItemService);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly memberService = inject(ProjectMemberService);
  private readonly userService = inject(UserService);
  private readonly commentService = inject(CommentService);
  private readonly attachmentService = inject(AttachmentService);
  private readonly cdr = inject(ChangeDetectorRef);

  id: string | null = null;
  task: WorkItem | null = null;
  project: Project | null = null;
  currentUserId = '';

  // Working state copy properties bound to controls
  description: string = '';
  notesText: string = '';
  status: string = '';
  type: string = '';
  assigneeId: string = '';
  priority: number = 1;
  estimatedEffort: number | null = null;

  currentUser: User | null = null;
  showDiscardConfirmDialog = false;
  deactivatePromiseResolve: ((value: boolean) => void) | null = null;

  originalState: {
    description: string;
    notes: string;
    status: string;
    type: string;
    assigneeId: string;
    priority: number;
    estimatedEffort: number | null;
  } | null = null;

  // Real User Metadata
  assigneeName: string = 'Unassigned';
  reporterName: string = 'Unknown';
  // projectMembers: list of users who belong to the current project (for assignment)
  projectUsers: { id: string; userName?: string; fullName: string; email?: string; role: 'MEMBER' | 'ADMIN' }[] = [];

  // Edit dialog
  showEditDialog = false;
  editTask: Partial<CreateWorkItemCommand & { notes?: string }> = {};

  // Delete dialog
  showDeleteDialog = false;

  // Comments & Attachments from DB
  comments: Comment[] = [];
  attachments: Attachment[] = [];
  newCommentText = '';

  statusOptions = [
    { label: 'TO DO', value: 'New' },
    { label: 'IN PROGRESS', value: 'Active' },
    { label: 'IN DEVELOPMENT', value: 'InDevelopment' },
    { label: 'IN TESTING', value: 'InTesting' },
    { label: 'REVIEW', value: 'Resolved' },
    { label: 'DONE', value: 'Closed' }
  ];

  priorityOptions = [
    { label: 'Highest', value: 1, icon: 'keyboard_double_arrow_up', colorClass: 'text-[#de350b]' },
    { label: 'High', value: 3, icon: 'keyboard_arrow_up', colorClass: 'text-[#ff5630]' },
    { label: 'Medium', value: 5, icon: 'keyboard_arrow_up', colorClass: 'text-[#0052cc]' },
    { label: 'Low', value: 7, icon: 'keyboard_arrow_down', colorClass: 'text-[#42526e]' },
    { label: 'Lowest', value: 9, icon: 'keyboard_double_arrow_down', colorClass: 'text-[#8993a4]' }
  ];

  typeOptions = [
    { label: 'Task', value: 'Task' },
    { label: 'Bug', value: 'Bug' },
    { label: 'Epic', value: 'Epic' },
    { label: 'Feature', value: 'Feature' },
    { label: 'User Story', value: 'UserStory' },
    { label: 'Subtask', value: 'Subtask' }
  ];

  get assigneeOptions() {
    const opts = [{ label: 'Unassigned', value: '' }];
    for (const u of this.projectUsers) {
      opts.push({ label: u.fullName, value: u.id });
    }
    return opts;
  }

  ngOnInit() {
    this.route.paramMap.subscribe({
      next: (params) => {
        this.id = params.get('id');
        this.loadTask();
      }
    });

    this.loadCurrentUser();

  }

  private loadCurrentUser(): void {
    this.userService.getCurrentUserProfile().subscribe({
      next: (user) => {
        this.currentUserId = user.id;
        this.currentUser = user;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading current user profile', err);
      },
    });
  }

  // Show edit dialog and prefill values
  openEditDialog(): void {
    if (!this.task || !this.canEditTask()) return;
    this.editTask = {
      description: this.task.description,
      priority: this.task.priority,
      type: this.task.type,
      status: this.task.status,
      dueDate: this.task.dueDate,
      estimatedEffort: this.task.estimatedEffort,
      projectId: this.task.projectId,
      assigneeId: this.task.assigneeId,
      notes: this.notesText,
    };
    this.showEditDialog = true;
  }

  cancelEditDialog(): void {
    this.showEditDialog = false;
  }

  confirmEditTask(): void {
    if (!this.id || !this.showEditDialog || !this.canEditTask()) return;
    const normalizedAssigneeId = this.editTask.assigneeId?.trim() || null;
    const workItemPayload = {
      ...this.editTask,
      assigneeId: normalizedAssigneeId,
    } as Partial<CreateWorkItemCommand & { notes?: string; assigneeId?: string | null }>;

    this.workItemService.updateWorkItem(this.id, workItemPayload).subscribe({
      next: () => {
        this.showEditDialog = false;
        this.loadTask();
      },
      error: (err) => {
        console.error('Error updating task', err);
      }
    });
  }

  openDeleteDialog(): void {
    this.showDeleteDialog = true;
  }

  cancelDeleteDialog(): void {
    this.showDeleteDialog = false;
  }

  confirmDeleteTask(): void {
    if (!this.id || !this.task || !this.canEditTask()) return;
    this.workItemService.deleteWorkItem(this.id).subscribe({
      next: () => {
        // Navigate back to project details
        if (this.task && this.task.projectId) {
          this.router.navigate(['/projects', this.task.projectId]);
        }
      },
      error: (err) => console.error('Error deleting task', err),
    });
  }

  // Test-friendly constructor: when tests instantiate the component directly
  // they pass doubles; allow optional overrides to support that pattern.
  constructor(route?: ActivatedRoute, workItemService?: WorkItemService) {
    if (route) {
      (this as any).route = route as ActivatedRoute;
    }
    if (workItemService) {
      (this as any).workItemService = workItemService as WorkItemService;
    }
  }

  loadTask() {
    if (!this.id) return;

    this.workItemService.getWorkItemById(this.id).subscribe({
      next: (data) => {
        this.task = data;
        this.description = data.description || '';
        this.status = data.status || '';
        this.type = data.type || 'Task';
        this.assigneeId = data.assigneeId || '';
        this.notesText = data.notes || '';
        this.priority = data.priority || 1;
        this.estimatedEffort = data.estimatedEffort !== undefined ? data.estimatedEffort : null;

        this.originalState = {
          description: this.description,
          notes: this.notesText,
          status: this.status,
          type: this.type,
          assigneeId: this.assigneeId,
          priority: this.priority,
          estimatedEffort: this.estimatedEffort
        };

        setTimeout(() => {
          const el = document.querySelector('textarea[placeholder="Issue summary"]');
          this.adjustTitleHeight(el);
        }, 0);

        // Load live Comments and Attachments
        this.loadComments();
        this.loadAttachments();
        this.loadReporter(data.authorId);

        if (data.projectId) {
          this.projectService.getProjectById(data.projectId).subscribe({
            next: (proj) => {
                  if (!proj) {
                    return;
                  }
                  this.project = proj;
                  this.loadProjectMembers(proj.id);
                  this.cdr.markForCheck();
                },
            error: (err) => console.error('Error loading project details', err)
          });
        }

        this.syncAssigneeName();

        // reporterName is driven by project owner (lead). If unavailable, keep 'Unknown'.

        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading task details', err);
        this.cdr.markForCheck();
      },
    });
  }

  private loadReporter(authorId: string): void {
    if (!authorId) {
      this.reporterName = 'Unknown';
      return;
    }

    this.userService.getUserById(authorId).subscribe({
      next: (user) => {
        this.reporterName = this.getUserDisplayName(user);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading task reporter', err);
        this.reporterName = authorId;
        this.cdr.markForCheck();
      },
    });
  }

  private getUserDisplayName(user: User): string {
    return `${(user.firstName || '').trim()} ${(user.lastName || '').trim()}`.trim() || user.userName || user.emailAddress || user.id;
  }

  private loadProjectMembers(projectId: string) {
    this.memberService.getProjectMembers(projectId).subscribe({
      next: (members) => {
        this.projectUsers = members.map((m) => ({
          id: m.userId,
          userName: m.userName,
          fullName: `${(m.firstName || '').trim()} ${(m.lastName || '').trim()}`.trim() || m.userName || m.emailAddress || 'Unknown',
          email: m.emailAddress,
          role: m.role,
        }));
        this.syncAssigneeName();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading project members', err);
      },
    });
  }

  private syncAssigneeName(): void {
    if (!this.assigneeId) {
      this.assigneeName = 'Unassigned';
      return;
    }

    const member = this.projectUsers.find((user) => user.id === this.assigneeId);
    this.assigneeName = member?.fullName || member?.userName || this.assigneeId;
  }

  canEditTask(): boolean {
    if (!this.task || !this.currentUserId) {
      return false;
    }

    if (this.task.authorId === this.currentUserId) {
      return true;
    }

    return this.projectUsers.some((member) => member.id === this.currentUserId && member.role === 'ADMIN');
  }

  updateStatus() {
    if (this.id && this.status) {
      this.workItemService.updateWorkItemStatus(this.id, { status: this.status }).subscribe({
        next: () => this.loadTask(),
        error: (err) => console.error(err),
      });
    }
  }

  assignUser() {
    if (this.id) {
      const normalizedAssigneeId = this.assigneeId.trim();
      this.workItemService.assignWorkItem(this.id, { assigneeId: normalizedAssigneeId ? normalizedAssigneeId : null }).subscribe({
        next: () => this.loadTask(),
        error: (err) => {
          console.error('Failed to update assignee', err);
        },
      });
    }
  }

  // Comments Management
  loadComments(): void {
    if (!this.id) return;
    this.commentService.getComments(this.id).subscribe({
      next: (list) => {
        this.comments = list;
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Error loading comments', err)
    });
  }

  addComment(): void {
    if (!this.id || !this.newCommentText.trim()) return;

    this.commentService.addComment(this.id, this.newCommentText.trim()).subscribe({
      next: () => {
        this.newCommentText = '';
        this.loadComments();
      },
      error: (err) => console.error('Error adding comment', err)
    });
  }

  deleteComment(commentId: string): void {
    this.commentService.deleteComment(commentId).subscribe({
      next: () => {
        this.loadComments();
      },
      error: (err) => console.error('Error deleting comment', err)
    });
  }

  canDeleteComment(comment: Comment): boolean {
    if (!this.currentUserId) {
      return false;
    }

    if (comment.authorId && comment.authorId === this.currentUserId) {
      return true;
    }

    return this.projectUsers.some((member) => member.id === this.currentUserId && member.role === 'ADMIN');
  }

  // Attachments Management
  loadAttachments(): void {
    if (!this.id) return;
    this.attachmentService.getAttachments(this.id).subscribe({
      next: (list) => {
        this.attachments = list;
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Error loading attachments', err)
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'Just now';
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return dateStr;
    }
  }

  // Visual Helpers
  getPriorityLabel(priority: number): string {
    if (priority <= 2) return 'Highest';
    if (priority <= 4) return 'High';
    if (priority <= 6) return 'Medium';
    if (priority <= 8) return 'Low';
    return 'Lowest';
  }

  getSeverity(status: string): string {
    switch (status) {
      case 'New':
        return 'info';
      case 'Active':
        return 'warn';
      case 'InDevelopment':
      case 'InTesting':
        return 'warn';
      case 'Resolved':
        return 'success';
      case 'Closed':
        return 'secondary';
      default:
        return 'info';
    }
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

  // --- Dynamic Edit & Save Mechanism ---

  get isDirty(): boolean {
    if (!this.originalState) return false;
    return (
      this.description !== this.originalState.description ||
      this.notesText !== this.originalState.notes ||
      this.status !== this.originalState.status ||
      this.type !== this.originalState.type ||
      this.assigneeId !== this.originalState.assigneeId ||
      this.priority !== this.originalState.priority ||
      this.estimatedEffort !== this.originalState.estimatedEffort
    );
  }

  get currentUserInitials(): string {
    if (!this.currentUser) return 'ME';
    const name = `${(this.currentUser.firstName || '').trim()} ${(this.currentUser.lastName || '').trim()}`.trim() || this.currentUser.userName || this.currentUser.emailAddress;
    return name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('') || 'ME';
  }

  adjustTitleHeight(textarea: any): void {
    if (textarea) {
      textarea.style.height = 'auto';
      textarea.style.height = textarea.scrollHeight + 'px';
    }
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (this.isDirty) {
      $event.returnValue = true;
    }
  }

  canDeactivate(): boolean | Promise<boolean> {
    if (!this.isDirty) {
      return true;
    }

    this.showDiscardConfirmDialog = true;
    this.cdr.markForCheck();

    return new Promise<boolean>((resolve) => {
      this.deactivatePromiseResolve = resolve;
    });
  }

  keepEditing(): void {
    this.showDiscardConfirmDialog = false;
    if (this.deactivatePromiseResolve) {
      this.deactivatePromiseResolve(false);
      this.deactivatePromiseResolve = null;
    }
  }

  discardChanges(): void {
    this.showDiscardConfirmDialog = false;
    if (this.originalState) {
      this.description = this.originalState.description;
      this.notesText = this.originalState.notes;
      this.status = this.originalState.status;
      this.type = this.originalState.type;
      this.assigneeId = this.originalState.assigneeId;
      this.priority = this.originalState.priority;
      this.estimatedEffort = this.originalState.estimatedEffort;
    }
    if (this.deactivatePromiseResolve) {
      this.deactivatePromiseResolve(true);
      this.deactivatePromiseResolve = null;
    }
  }

  saveAllChanges(): void {
    if (!this.id || !this.isDirty || !this.canEditTask()) return;

    const normalizedAssigneeId = this.assigneeId?.trim() || null;
    const workItemPayload = {
      description: this.description.trim(),
      priority: this.priority,
      status: this.status,
      type: this.type,
      assigneeId: normalizedAssigneeId,
      estimatedEffort: this.estimatedEffort,
      notes: this.notesText.trim()
    } as any;

    this.workItemService.updateWorkItem(this.id, workItemPayload).subscribe({
      next: () => {
        this.toastService.success('Task updated successfully.');
        this.loadTask();
      },
      error: (err) => {
        this.toastService.error('Failed to update task.');
        console.error('Error saving task modifications', err);
      }
    });
  }
}
