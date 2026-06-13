import { CommonModule } from '@angular/common';
import { Component, DestroyRef, Input, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { WorkItem, WorkItemService, CreateWorkItemCommand } from '../../../core/services/work-item.service';
import { ProjectMemberService } from '../../../core/services/project-member.service';
import { DialogComponent } from '../../../shared/components/ui/dialog';
import { ButtonComponent } from '../../../shared/components/ui/button';
import { InputComponent } from '../../../shared/components/ui/input';
import { SelectComponent } from '../../../shared/components/ui/select';

type ProjectUser = {
    id: string;
    userName?: string;
    fullName: string;
};

@Component({
    selector: 'app-project-backlog-panel',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, DialogComponent, ButtonComponent, InputComponent, SelectComponent],
    templateUrl: './project-backlog-panel.html',
})
export class ProjectBacklogPanelComponent implements OnInit {
    private readonly workItemService = inject(WorkItemService);
    private readonly memberService = inject(ProjectMemberService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly router = inject(Router);

    private _projectId = '';

    get assigneeOptions() {
        const opts = [{ label: 'Unassigned', value: '' }];
        for (const u of this.projectUsers()) {
            opts.push({ label: u.fullName, value: u.id });
        }
        return opts;
    }

    @Input()
    set projectId(value: string) {
        this._projectId = value;
        if (this.initialized && value) {
            this.loadTasks();
            this.loadMembers();
        }
    }

    get projectId(): string {
        return this._projectId;
    }

    @Input() projectCode = '';

    tasks = signal<WorkItem[]>([]);
    projectUsers = signal<ProjectUser[]>([]);
    loadingTasks = signal(false);
    showCreateDialog = signal(false);
    showDeleteTaskDialog = signal(false);
    creating = signal(false);
    deleting = signal(false);
    taskToDelete = signal<WorkItem | null>(null);
    deleteErrorMessage = signal('');

    newTask: Partial<CreateWorkItemCommand> = this.emptyTask();

    typeOptions = [
        { label: 'Task', value: 'Task' },
        { label: 'Bug', value: 'Bug' },
        { label: 'Epic', value: 'Epic' },
        { label: 'Feature', value: 'Feature' },
        { label: 'User Story', value: 'UserStory' },
        { label: 'Subtask', value: 'Subtask' },
    ];

    statusOptions = [
        { label: 'New', value: 'New' },
        { label: 'Active', value: 'Active' },
        { label: 'In Development', value: 'InDevelopment' },
        { label: 'In Testing', value: 'InTesting' },
        { label: 'Resolved', value: 'Resolved' },
        { label: 'Closed', value: 'Closed' },
    ];

    private initialized = false;

    ngOnInit(): void {
        this.initialized = true;
        if (this.projectId) {
            this.loadTasks();
            this.loadMembers();
        }
    }

    get showCreateDialogVisible() { return this.showCreateDialog(); }
    set showCreateDialogVisible(v: boolean) { this.showCreateDialog.set(v); }

    get showDeleteTaskDialogVisible() { return this.showDeleteTaskDialog(); }
    set showDeleteTaskDialogVisible(v: boolean) { this.showDeleteTaskDialog.set(v); }

    private emptyTask(): Partial<CreateWorkItemCommand> {
        return { description: '', priority: 1, type: 'Task', status: 'New', assigneeId: '' };
    }

    loadTasks(): void {
        if (!this.projectId) {
            return;
        }

        this.loadingTasks.set(true);
        this.workItemService
            .getWorkItems(this.projectId)
            .pipe(finalize(() => this.loadingTasks.set(false)), takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (data) => {
                    this.tasks.set(data);
                },
                error: (err) => {
                    console.error(err);
                },
            });
    }

    loadMembers(): void {
        if (!this.projectId) {
            this.projectUsers.set([]);
            return;
        }

        this.memberService
            .getProjectMembers(this.projectId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (members) => {
                    this.projectUsers.set(members.map((member) => ({
                        id: member.userId,
                        userName: member.userName,
                        fullName: `${(member.firstName || '').trim()} ${(member.lastName || '').trim()}`.trim() || member.userName || member.emailAddress || 'Unknown',
                    })));
                },
                error: (err) => {
                    console.error(err);
                },
            });
    }

    openCreateDialog(): void {
        this.newTask = this.emptyTask();
        this.showCreateDialog.set(true);
    }

    navigateToCreate(): void {
        this.router.navigate(['/tasks/new'], { queryParams: { projectId: this.projectId } });
    }

    createTask(): void {
        if (!this.projectId) {
            return;
        }

        const description = this.newTask.description?.trim();
        if (!description) {
            return;
        }

        const assigneeId = this.newTask.assigneeId?.trim();
        const command: CreateWorkItemCommand = {
            ...(this.newTask as CreateWorkItemCommand),
            description,
            projectId: this.projectId,
            assigneeId: assigneeId || null,
        };

        this.creating.set(true);
        this.workItemService
            .createWorkItem(command)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.creating.set(false);
                    this.showCreateDialog.set(false);
                    this.loadTasks();
                },
                error: (err) => {
                    console.error(err);
                    this.creating.set(false);
                },
            });
    }

    openDeleteTaskDialog(task: WorkItem, event?: MouseEvent): void {
        event?.preventDefault();
        event?.stopPropagation();
        this.taskToDelete.set(task);
        this.deleteErrorMessage.set('');
        this.showDeleteTaskDialog.set(true);
    }

    cancelDeleteTaskDialog(): void {
        this.showDeleteTaskDialog.set(false);
        this.taskToDelete.set(null);
        this.deleteErrorMessage.set('');
    }

    confirmDeleteTask(): void {
        const task = this.taskToDelete();
        if (!task || this.deleting()) {
            return;
        }

        this.deleting.set(true);
        this.deleteErrorMessage.set('');

        this.workItemService
            .deleteWorkItem(task.id)
            .pipe(
                takeUntilDestroyed(this.destroyRef),
                finalize(() => this.deleting.set(false)),
            )
            .subscribe({
                next: () => {
                    this.cancelDeleteTaskDialog();
                    this.loadTasks();
                },
                error: (err) => {
                    console.error(err);
                    this.deleteErrorMessage.set(this.mapTaskDeleteError(err));
                },
            });
    }

    private mapTaskDeleteError(err: unknown): string {
        if (err instanceof HttpErrorResponse) {
            if (err.status === 401) {
                return 'Your session expired. Sign in again, then retry deleting the issue.';
            }
            if (err.status === 403) {
                return 'You are not allowed to delete this issue.';
            }
            if (err.status === 0) {
                return 'Could not reach the API. Check your network connection.';
            }

            const apiMessage = this.extractApiMessage(err);
            if (apiMessage) {
                return apiMessage;
            }
        }

        return 'Could not delete the issue. Please try again.';
    }

    private extractApiMessage(err: HttpErrorResponse): string | null {
        const body = err.error;
        if (body && typeof body === 'object') {
            if ('detail' in body) {
                const detail = (body as { detail?: unknown }).detail;
                if (typeof detail === 'string' && detail.trim() !== '') {
                    return detail;
                }
            }

            if ('message' in body) {
                const message = (body as { message?: unknown }).message;
                if (typeof message === 'string' && message.trim() !== '') {
                    return message;
                }
            }
        }

        if (typeof body === 'string' && body.trim() !== '') {
            return body;
        }

        return null;
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

    getAssigneeName(task: WorkItem): string {
        if (!task.assigneeId) {
            return 'Unassigned';
        }

        const user = this.projectUsers().find((member) => member.id === task.assigneeId);
        return user?.fullName || user?.userName || task.assigneeId;
    }
}
