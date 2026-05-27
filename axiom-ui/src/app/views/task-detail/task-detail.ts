import { Component, OnInit, inject, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
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

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ButtonComponent, DialogComponent],
  templateUrl: './task-detail.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TaskDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
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
  status: string = '';
  assigneeId: string = '';
  currentUserId = '';

  // Real User Metadata
  assigneeName: string = 'Unassigned';
  reporterName: string = 'Unknown';
  // projectMembers: list of users who belong to the current project (for assignment)
  projectUsers: { id: string; userName?: string; fullName: string; email?: string; role: 'MEMBER' | 'ADMIN' }[] = [];

  // Description / Notes Editor
  editingNotes: boolean = false;
  notesText: string = '';

  // Edit dialog
  showEditDialog = false;
  editTask: Partial<CreateWorkItemCommand & { notes?: string; estimatedEffort?: number }> = {};

  // Delete dialog
  showDeleteDialog = false;

  // Comments & Attachments from DB
  comments: Comment[] = [];
  attachments: Attachment[] = [];
  newCommentText = '';

  statusOptions = [
    { label: 'TO DO', value: 'New' },
    { label: 'IN PROGRESS', value: 'Active' },
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
    } as Partial<CreateWorkItemCommand & { notes?: string; estimatedEffort?: number; assigneeId?: string | null }>;

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
  // they pass mocks; allow optional overrides to support that pattern.
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
        this.status = data.status;
        this.assigneeId = data.assigneeId || '';
        this.notesText = data.notes || '';

        // Load live Comments and Attachments
        this.loadComments();
        this.loadAttachments();

        if (data.projectId) {
          this.projectService.getProjectById(data.projectId).subscribe({
            next: (proj) => {
                  if (!proj) {
                    return;
                  }
                  this.project = proj;
                  // set reporter to project owner (lead) when available
                  this.reporterName = proj.ownerName || this.reporterName;
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
    this.assigneeName = member?.fullName || member?.userName || 'Unassigned';
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

  // Editable Notes
  saveNotes(): void {
    if (!this.id || !this.canEditTask()) return;
    this.workItemService.updateWorkItemNotes(this.id, this.notesText.trim()).subscribe({
      next: () => {
        this.editingNotes = false;
        this.loadTask();
      },
      error: (err) => console.error('Error updating notes', err)
    });
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
}
