import {
  Component,
  OnInit,
  inject,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import {
  WorkItemService,
  WorkItem,
  CreateWorkItemCommand,
  TaskRelationship,
} from '../../core/services/work-item.service';
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
import { extractErrorMessage } from '../../core/utils/error-utils';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ButtonComponent,
    DialogComponent,
    SelectComponent,
  ],
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
  /** Parent item ID – required for all non-Epic types during task creation */
  parentId: string = '';

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
    parentId: string;
  } | null = null;

  // Real User Metadata
  assigneeName: string = 'Unassigned';
  reporterName: string = 'Unknown';
  // projectMembers: list of users who belong to the current project (for assignment)
  projectUsers: {
    id: string;
    userName?: string;
    fullName: string;
    email?: string;
    role: 'MEMBER' | 'ADMIN';
  }[] = [];

  // Edit dialog
  showEditDialog = false;
  editTask: Partial<CreateWorkItemCommand & { notes?: string }> = {};

  // Delete dialog
  showDeleteDialog = false;

  // Comments & Attachments from DB
  comments: Comment[] = [];
  attachments: Attachment[] = [];
  newCommentText = '';
  selectedAttachment: File | null = null;
  isUploadingAttachment = false;

  // Relationships
  relationships: TaskRelationship[] = [];
  allProjectTasks: WorkItem[] = [];
  newLinkType = 'RelatesTo';
  newLinkTargetId = '';
  typeHierarchy: Record<string, string[]> = {};
  saving = false;

  linkTypeOptions = [
    { label: 'Parent', value: 'ParentOf' },
    { label: 'Child', value: 'ChildOf' },
    { label: 'Depends On (Blocked By)', value: 'BlockedBy' },
    { label: 'Blocks', value: 'Blocks' },
    { label: 'Relates To', value: 'RelatesTo' }
  ];

  statusOptions = [
    { label: 'TO DO', value: 'New' },
    { label: 'IN PROGRESS', value: 'Active' },
    { label: 'IN DEVELOPMENT', value: 'InDevelopment' },
    { label: 'IN TESTING', value: 'InTesting' },
    { label: 'REVIEW', value: 'Resolved' },
    { label: 'DONE', value: 'Closed' },
  ];

  priorityOptions = [
    { label: 'Highest', value: 1, icon: 'keyboard_double_arrow_up', colorClass: 'text-[#de350b]' },
    { label: 'High', value: 3, icon: 'keyboard_arrow_up', colorClass: 'text-[#ff5630]' },
    { label: 'Medium', value: 5, icon: 'keyboard_arrow_up', colorClass: 'text-[#0052cc]' },
    { label: 'Low', value: 7, icon: 'keyboard_arrow_down', colorClass: 'text-[#42526e]' },
    { label: 'Lowest', value: 9, icon: 'keyboard_double_arrow_down', colorClass: 'text-[#8993a4]' },
  ];

  typeOptions = [
    { label: 'Task', value: 'Task' },
    { label: 'Bug', value: 'Bug' },
    { label: 'Epic', value: 'Epic' },
    { label: 'Feature', value: 'Feature' },
    { label: 'User Story', value: 'UserStory' },
    { label: 'Subtask', value: 'Subtask' },
  ];

  get assigneeOptions() {
    const opts = [{ label: 'Unassigned', value: '' }];

    if (
      this.currentUser &&
      !this.projectUsers.some((user) => user.id === this.currentUser!.id)
    ) {
      opts.push({
        label: `${this.getUserDisplayName(this.currentUser)} (You)`,
        value: this.currentUser.id,
      });
    }

    for (const u of this.projectUsers) {
      opts.push({
        label: `${u.fullName}${u.id === this.currentUserId ? ' (You)' : ''}`,
        value: u.id,
      });
    }
    return opts;
  }

  ngOnInit() {
    this.route.paramMap.subscribe({
      next: (params) => {
        this.id = params.get('id');
        this.loadTask();
      },
    });

    this.loadCurrentUser();

    this.workItemService.getTypeHierarchy().subscribe({
      next: (h) => {
        this.typeHierarchy = h;
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Failed to load type hierarchy', err),
    });
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
      },
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
      error: (err) => {
        this.toastService.error(extractErrorMessage(err, 'Failed to delete issue.'));
        console.error('Error deleting task', err);
      },
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

    if (this.id === 'new') {
      const qProjectId = this.route.snapshot?.queryParamMap?.get('projectId');
      const currentProj = qProjectId
        ? { id: qProjectId } as Project
        : (typeof this.projectService.getCurrentProject === 'function' ? this.projectService.getCurrentProject() : null);

      const projectId = currentProj?.id || '';

      this.task = {
        id: 'new',
        controlNo: 0,
        description: '',
        priority: 5,
        type: 'Task',
        status: 'New',
        dueDate: '',
        estimatedEffort: null,
        projectId: projectId,
        authorId: this.currentUserId,
        assigneeId: '',
      } as any;

      this.description = '';
      this.status = 'New';
      this.type = 'Task';
      this.assigneeId = '';
      this.notesText = '';
      this.priority = 5;
      this.estimatedEffort = null;
      this.parentId = this.route.snapshot?.queryParamMap?.get('parentId') || '';

      this.originalState = {
        description: this.description,
        notes: this.notesText,
        status: this.status,
        type: this.type,
        assigneeId: this.assigneeId,
        priority: this.priority,
        estimatedEffort: this.estimatedEffort,
        parentId: '',
      };

      if (projectId) {
        this.projectService.getProjectById(projectId).subscribe({
          next: (proj) => {
            if (proj) {
              this.project = proj;
              this.projectService.setCurrentProject(proj);
              this.loadProjectMembers(proj.id);
              this.loadProjectTasks(proj.id);
            }
            this.cdr.markForCheck();
          },
          error: (err) => console.error('Error loading project details', err),
        });
      }

      this.reporterName = 'Unknown';
      this.userService.getCurrentUserProfile().subscribe({
        next: (user) => {
          this.currentUserId = user.id;
          this.currentUser = user;
          this.reporterName = this.getUserDisplayName(user);
          this.cdr.markForCheck();
        },
      });

      this.cdr.markForCheck();
      return;
    }

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
          estimatedEffort: this.estimatedEffort,
          parentId: '',
        };

        setTimeout(() => {
          const el = document.querySelector('#issue-summary');
          this.adjustTitleHeight(el);
        }, 0);

        // Load live Comments and Attachments
        this.loadComments();
        this.loadAttachments();
        this.loadReporter(data.authorId);
        this.loadTaskRelationships();

        if (data.projectId) {
          this.projectService.getProjectById(data.projectId).subscribe({
            next: (proj) => {
              if (!proj) {
                return;
              }
              this.project = proj;
              this.projectService.setCurrentProject(proj);
              this.loadProjectMembers(proj.id);
              this.loadProjectTasks(proj.id);
              this.cdr.markForCheck();
            },
            error: (err) => console.error('Error loading project details', err),
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
    return (
      `${(user.firstName || '').trim()} ${(user.lastName || '').trim()}`.trim() ||
      user.userName ||
      user.emailAddress ||
      user.id
    );
  }

  private loadProjectMembers(projectId: string) {
    this.memberService.getProjectMembers(projectId).subscribe({
      next: (members) => {
        this.projectUsers = members.map((m) => ({
          id: m.userId,
          userName: m.userName,
          fullName:
            `${(m.firstName || '').trim()} ${(m.lastName || '').trim()}`.trim() ||
            m.userName ||
            m.emailAddress ||
            'Unknown',
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
    if (this.id === 'new') {
      return true;
    }

    if (!this.task || !this.currentUserId) {
      return false;
    }

    if (this.task.authorId === this.currentUserId) {
      return true;
    }

    return this.projectUsers.some(
      (member) => member.id === this.currentUserId && member.role === 'ADMIN',
    );
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
      this.workItemService
        .assignWorkItem(this.id, { assigneeId: normalizedAssigneeId ? normalizedAssigneeId : null })
        .subscribe({
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
      error: (err) => console.error('Error loading comments', err),
    });
  }

  addComment(): void {
    if (!this.id || !this.newCommentText.trim()) return;

    this.commentService.addComment(this.id, this.newCommentText.trim()).subscribe({
      next: () => {
        this.newCommentText = '';
        this.loadComments();
      },
      error: (err) => console.error('Error adding comment', err),
    });
  }

  deleteComment(commentId: string): void {
    this.commentService.deleteComment(commentId).subscribe({
      next: () => {
        this.loadComments();
      },
      error: (err) => console.error('Error deleting comment', err),
    });
  }

  canDeleteComment(comment: Comment): boolean {
    if (!this.currentUserId) {
      return false;
    }

    if (comment.authorId && comment.authorId === this.currentUserId) {
      return true;
    }

    return this.projectUsers.some(
      (member) => member.id === this.currentUserId && member.role === 'ADMIN',
    );
  }

  // Attachments Management
  loadAttachments(): void {
    if (!this.id) return;
    this.attachmentService.getAttachments(this.id).subscribe({
      next: (list) => {
        this.attachments = list;
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Error loading attachments', err),
    });
  }

  onAttachmentSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;

    if (file && file.size > 10 * 1024 * 1024) {
      this.toastService.error('Maximum allowed file size is 10 MB.');
      input.value = '';
      return;
    }

    this.selectedAttachment = file;
  }

  uploadAttachment(): void {
    if (!this.id || !this.selectedAttachment || this.isUploadingAttachment) return;

    this.isUploadingAttachment = true;
    this.attachmentService.uploadAttachment(this.id, this.selectedAttachment).subscribe({
      next: () => {
        this.selectedAttachment = null;
        this.isUploadingAttachment = false;
        this.toastService.success('Attachment uploaded successfully.');
        this.loadAttachments();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isUploadingAttachment = false;
        const message = err?.error?.detail || err?.error?.message || 'Failed to upload attachment.';
        this.toastService.error(message);
        console.error('Error uploading attachment', err);
        this.cdr.markForCheck();
      },
    });
  }

  downloadAttachment(file: Attachment): void {
    this.attachmentService.downloadAttachment(file.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = file.fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.toastService.error('Failed to download attachment.');
        console.error('Error downloading attachment', err);
      },
    });
  }

  deleteAttachment(file: Attachment): void {
    this.attachmentService.deleteAttachment(file.id).subscribe({
      next: () => {
        this.toastService.success('Attachment deleted.');
        this.loadAttachments();
      },
      error: (err) => {
        this.toastService.error('Failed to delete attachment.');
        console.error('Error deleting attachment', err);
      },
    });
  }

  canDeleteAttachment(file: Attachment): boolean {
    if (!this.currentUserId) {
      return false;
    }

    if (file.uploadedBy === this.currentUserId) {
      return true;
    }

    return this.projectUsers.some(
      (member) => member.id === this.currentUserId && member.role === 'ADMIN',
    );
  }

  formatFileSize(size: number): string {
    if (!size) return '0 B';
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return 'Just now';
    try {
      const date = new Date(dateStr);
      return (
        date.toLocaleDateString() +
        ' ' +
        date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      );
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
      this.estimatedEffort !== this.originalState.estimatedEffort ||
      this.parentId !== this.originalState.parentId
    );
  }

  get currentUserInitials(): string {
    if (!this.currentUser) return 'ME';
    const name =
      `${(this.currentUser.firstName || '').trim()} ${(this.currentUser.lastName || '').trim()}`.trim() ||
      this.currentUser.userName ||
      this.currentUser.emailAddress;
    return (
      name
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0]?.toUpperCase())
        .join('') || 'ME'
    );
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
      this.parentId = this.originalState.parentId;
    }
    if (this.deactivatePromiseResolve) {
      this.deactivatePromiseResolve(true);
      this.deactivatePromiseResolve = null;
    }
  }

  saveAllChanges(): void {
    if (!this.isDirty || this.saving) return;

    this.saving = true;
    this.cdr.markForCheck();

    if (this.id === 'new') {
      if (!this.description.trim()) {
        this.toastService.error('Issue summary is required.');
        this.saving = false;
        this.cdr.markForCheck();
        setTimeout(() => {
          document.querySelector<HTMLTextAreaElement>('#issue-summary')?.focus();
        });
        return;
      }

      // Enforce parent selection for all non-Epic types
      const isEpic = this.type === 'Epic';
      if (!isEpic && !this.parentId) {
        this.toastService.error('You must select a parent (Feature or User Story) before saving.');
        this.saving = false;
        this.cdr.markForCheck();
        return;
      }

      const normalizedAssigneeId = this.assigneeId?.trim() || null;
      const createPayload: CreateWorkItemCommand = {
        description: this.description.trim(),
        priority: this.priority,
        status: this.status,
        type: this.type,
        projectId: this.task?.projectId || '',
        assigneeId: normalizedAssigneeId,
        estimatedEffort: this.estimatedEffort,
      };

      this.workItemService.createWorkItem(createPayload).subscribe({
        next: (newId) => {
          const finalize = () => {
            this.saving = false;
            this.toastService.success('Task created successfully.');
            this.originalState = null; // Bypass pendingChangesGuard
            this.router.navigate(['/tasks', newId]);
          };

          const afterNotes = () => {
            // Create parent relationship if a parent was selected
            if (this.parentId) {
              this.workItemService.createRelationship({
                sourceId: newId,
                targetId: this.parentId,
                linkType: 'ChildOf'
              }).subscribe({
                next: finalize,
                error: (err) => {
                  this.toastService.error(extractErrorMessage(err, 'Issue created but parent link failed.'));
                  console.error('Error creating parent relationship', err);
                  finalize();
                }
              });
            } else {
              finalize();
            }
          };

          const notes = this.notesText.trim();
          if (notes) {
            this.workItemService.updateWorkItemNotes(newId, notes).subscribe({
              next: afterNotes,
              error: (err) => {
                console.error('Error saving notes for new task', err);
                afterNotes();
              }
            });
          } else {
            afterNotes();
          }
        },
        error: (err) => {
          this.saving = false;
          this.toastService.error(extractErrorMessage(err, 'Failed to create issue.'));
          console.error('Error creating task', err);
          this.cdr.markForCheck();
        },
      });
      return;
    }

    if (!this.id || !this.canEditTask()) {
      this.saving = false;
      this.cdr.markForCheck();
      return;
    }

    const normalizedAssigneeId = this.assigneeId?.trim() || null;
    const workItemPayload = {
      description: this.description.trim(),
      priority: this.priority,
      status: this.status,
      type: this.type,
      assigneeId: normalizedAssigneeId,
      estimatedEffort: this.estimatedEffort,
      notes: this.notesText.trim(),
    } as any;

    this.workItemService.updateWorkItem(this.id, workItemPayload).subscribe({
      next: () => {
        this.saving = false;
        this.toastService.success('Task updated successfully.');
        this.loadTask();
      },
      error: (err) => {
        this.saving = false;
        this.toastService.error(extractErrorMessage(err, 'Failed to update issue.'));
        console.error('Error saving task modifications', err);
        this.cdr.markForCheck();
      },
    });
  }

  loadTaskRelationships(): void {
    if (!this.id || this.id === 'new') return;
    this.workItemService.getRelationshipsByWorkItem(this.id).subscribe({
      next: (list) => {
        this.relationships = list || [];
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Error loading relationships', err)
    });
  }

  loadProjectTasks(projectId: string): void {
    this.workItemService.getWorkItems(projectId).subscribe({
      next: (list) => {
        this.allProjectTasks = list || [];
        this.cdr.markForCheck();
      },
      error: (err) => console.error('Error loading project tasks', err)
    });
  }

  addRelationship(): void {
    if (!this.id || !this.newLinkTargetId) return;
    this.workItemService.createRelationship({
      sourceId: this.id,
      targetId: this.newLinkTargetId,
      linkType: this.newLinkType
    }).subscribe({
      next: () => {
        this.toastService.success('Relationship added successfully.');
        this.newLinkTargetId = '';
        this.loadTaskRelationships();
      },
      error: (err) => {
        const errMsg = err?.error?.detail || err?.error?.message || 'Failed to add relationship.';
        this.toastService.error(errMsg);
      }
    });
  }

  removeRelationship(relId: string): void {
    this.workItemService.deleteRelationship(relId).subscribe({
      next: () => {
        this.toastService.success('Relationship removed.');
        this.loadTaskRelationships();
      },
      error: (err) => {
        console.error('Error deleting relationship', err);
        this.toastService.error('Failed to delete relationship.');
      }
    });
  }

  get parentRelations(): any[] {
    return this.relationships
      .filter(r => r.linkType === 'ChildOf' && r.sourceId === this.id)
      .map(r => ({
        id: r.id,
        task: this.getTaskById(r.targetId),
        targetId: r.targetId
      }));
  }

  get childrenRelations(): any[] {
    return this.relationships
      .filter(r => r.linkType === 'ChildOf' && r.targetId === this.id)
      .map(r => ({
        id: r.id,
        task: this.getTaskById(r.sourceId),
        targetId: r.sourceId
      }));
  }

  get blockerRelations(): any[] {
    return this.relationships
      .filter(r => r.linkType === 'BlockedBy' && r.sourceId === this.id)
      .map(r => ({
        id: r.id,
        task: this.getTaskById(r.targetId),
        targetId: r.targetId
      }));
  }

  get dependentRelations(): any[] {
    return this.relationships
      .filter(r => r.linkType === 'BlockedBy' && r.targetId === this.id)
      .map(r => ({
        id: r.id,
        task: this.getTaskById(r.sourceId),
        targetId: r.sourceId
      }));
  }

  get relatedRelations(): any[] {
    return this.relationships
      .filter(r => r.linkType === 'RelatesTo')
      .map(r => {
        const otherId = r.sourceId === this.id ? r.targetId : r.sourceId;
        return {
          id: r.id,
          task: this.getTaskById(otherId),
          targetId: otherId
        };
      });
  }

  getTaskById(taskId: string): WorkItem | null {
    if (this.task && this.task.id === taskId) return this.task;
    return this.allProjectTasks.find(x => x.id === taskId) || null;
  }

  getTaskLabel(task: WorkItem | null, taskId: string): string {
    if (!task) return taskId;
    const code = this.project?.code?.toUpperCase() || 'AX';
    return `${code}-${task.controlNo}: ${task.description}`;
  }

  get availableTasksToLink(): WorkItem[] {
    if (!this.project || !this.task) return [];
    const linkedIds = new Set<string>();
    this.relationships.forEach(r => {
      linkedIds.add(r.sourceId);
      linkedIds.add(r.targetId);
    });
    return this.allProjectTasks.filter(t => t.id !== this.id && !linkedIds.has(t.id));
  }

  /**
   * Returns work items that can be a parent of the currently-selected type.
   * Based on the typeHierarchy fetched from the backend, falls back to level checks.
   */
  get availableParentItems(): WorkItem[] {
    if (Object.keys(this.typeHierarchy).length === 0) {
      const levelMap: Record<string, number> = {
        Epic: 1, Feature: 2, UserStory: 3, Task: 4, Bug: 4, Subtask: 5
      };
      const childLevel = levelMap[this.type] ?? 99;
      return this.allProjectTasks.filter(t => {
        const parentLevel = levelMap[t.type] ?? 0;
        return parentLevel < childLevel;
      });
    }
    return this.allProjectTasks.filter(t => {
      const allowedChildren = this.typeHierarchy[t.type] ?? [];
      return allowedChildren.includes(this.type);
    });
  }

  /** Called when the type selector changes in new-task mode to reset incompatible parent. */
  onTypeChange(): void {
    if (this.id !== 'new') return;
    // Clear parent if selected parent is no longer valid for the new type
    if (this.parentId) {
      const stillValid = this.availableParentItems.some(t => t.id === this.parentId);
      if (!stillValid) {
        this.parentId = '';
      }
    }
  }
}
