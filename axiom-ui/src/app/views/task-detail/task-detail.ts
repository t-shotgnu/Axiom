import { Component, OnInit, inject, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { WorkItemService, WorkItem } from '../../core/services/work-item.service';
import { ProjectService, Project } from '../../core/services/project.service';
import { AuthService } from '../../core/services/auth.service';
import { UserService, User } from '../../core/services/user.service';
import { CommentService, Comment } from '../../core/services/comment.service';
import { AttachmentService, Attachment } from '../../core/services/attachment.service';
import { ButtonComponent } from '../../shared/components/ui/button';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ButtonComponent],
  templateUrl: './task-detail.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TaskDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly workItemService = inject(WorkItemService);
  private readonly projectService = inject(ProjectService);
  private readonly userService = inject(UserService);
  private readonly commentService = inject(CommentService);
  private readonly attachmentService = inject(AttachmentService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  id: string | null = null;
  task: WorkItem | null = null;
  project: Project | null = null;
  status: string = '';
  assigneeId: string = '';
  userEmail: string = '';

  // Real User Metadata
  assigneeName: string = 'Unassigned';
  reporterName: string = 'Sarah Adams';
  usersList: User[] = [];

  // Description / Notes Editor
  editingNotes: boolean = false;
  notesText: string = '';

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

    const token = this.authService.getToken();
    this.userEmail = token ? this.decodeEmailFromJwt(token) : '';

    this.userService.getAllUsers().subscribe({
      next: (list) => {
        this.usersList = list;
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Error loading users list', err)
    });
  }

  private decodeEmailFromJwt(token: string): string {
    try {
      const payload = JSON.parse(atob(token.split('.')[1] as string));
      return (payload.sub as string) ?? '';
    } catch {
      return '';
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
              this.project = proj;
              this.cdr.markForCheck();
            },
            error: (err) => console.error('Error loading project details', err)
          });
        }

        if (data.assigneeId) {
          this.userService.getUserById(data.assigneeId).subscribe({
            next: (u) => {
              this.assigneeName = u.userName;
              this.cdr.markForCheck();
            },
            error: () => {
              this.assigneeName = 'Unassigned';
              this.cdr.markForCheck();
            }
          });
        } else {
          this.assigneeName = 'Unassigned';
        }

        if (data.authorId) {
          this.userService.getUserById(data.authorId).subscribe({
            next: (u) => {
              this.reporterName = u.userName;
              this.cdr.markForCheck();
            },
            error: () => {
              this.reporterName = 'Sarah Adams';
              this.cdr.markForCheck();
            }
          });
        } else {
          this.reporterName = 'Sarah Adams';
        }

        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading task details', err);
        this.cdr.markForCheck();
      },
    });
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
      this.workItemService.assignWorkItem(this.id, { assigneeId: this.assigneeId }).subscribe({
        next: () => this.loadTask(),
        error: (err) => console.error(err),
      });
    }
  }

  // Editable Notes
  saveNotes(): void {
    if (!this.id) return;
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
