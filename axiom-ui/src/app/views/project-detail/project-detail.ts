import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ProjectService, Project } from '../../core/services/project.service';
import { WorkItemService, WorkItem, CreateWorkItemCommand } from '../../core/services/work-item.service';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../../shared/components/ui/button';
import { CardComponent } from '../../shared/components/ui/card';
import { DialogComponent } from '../../shared/components/ui/dialog';
import { TextComponent } from '../../shared/components/ui/text';
import { InputComponent } from '../../shared/components/ui/input';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [
    RouterModule,
    FormsModule,
    CommonModule,
    ButtonComponent,
    CardComponent,
    DialogComponent,
    TextComponent,
    InputComponent,
  ],
  templateUrl: './project-detail.html',
})
export class ProjectDetailComponent {
  private route = inject(ActivatedRoute);
  private projectService = inject(ProjectService);
  private workItemService = inject(WorkItemService);
  private destroyRef = inject(DestroyRef);

  project = signal<Project | null>(null);
  tasks = signal<WorkItem[]>([]);
  loadingProject = signal(false);
  loadingTasks = signal(false);
  showCreateDialog = signal(false);
  creating = signal(false);

  newTask: Partial<CreateWorkItemCommand> = this.emptyTask();

  typeOptions = [
    { label: 'Task',       value: 'Task',      icon: 'pi pi-file',               color: '#eab308' },
    { label: 'Bug',        value: 'Bug',        icon: 'pi pi-exclamation-circle', color: '#ef4444' },
    { label: 'Epic',       value: 'Epic',       icon: 'pi pi-crown',              color: '#a855f7' },
    { label: 'Feature',    value: 'Feature',    icon: 'pi pi-star',               color: '#3b82f6' },
    { label: 'User Story', value: 'UserStory',  icon: 'pi pi-user',               color: '#22c55e' },
    { label: 'Subtask',    value: 'Subtask',    icon: 'pi pi-sitemap',            color: '#71717a' },
  ];

  statusOptions = [
    { label: 'New', value: 'New' },
    { label: 'Active', value: 'Active' },
    { label: 'Resolved', value: 'Resolved' },
    { label: 'Closed', value: 'Closed' },
  ];

  constructor() {
    const projectId = this.route.snapshot.paramMap.get('id')!;
    this.loadProject(projectId);
    this.loadTasks(projectId);
  }

  get showCreateDialogVisible() { return this.showCreateDialog(); }
  set showCreateDialogVisible(v: boolean) { this.showCreateDialog.set(v); }

  private emptyTask(): Partial<CreateWorkItemCommand> {
    return { description: '', priority: 1, type: 'Task', status: 'New' };
  }

  loadProject(id: string) {
    this.loadingProject.set(true);
    this.projectService.getProjectById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (p) => { this.project.set(p); this.loadingProject.set(false); },
        error: (err) => { console.error(err); this.loadingProject.set(false); },
      });
  }

  loadTasks(projectId: string) {
    this.loadingTasks.set(true);
    this.workItemService.getWorkItems(projectId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => { this.tasks.set(data); this.loadingTasks.set(false); },
        error: (err) => { console.error(err); this.loadingTasks.set(false); },
      });
  }

  openCreateDialog() {
    this.newTask = this.emptyTask();
    this.showCreateDialog.set(true);
  }

  createTask() {
    const projectId = this.route.snapshot.paramMap.get('id')!;
    const command: CreateWorkItemCommand = {
      ...(this.newTask as CreateWorkItemCommand),
      projectId,
    };

    this.creating.set(true);
    this.workItemService.createWorkItem(command)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.creating.set(false);
          this.showCreateDialog.set(false);
          this.loadTasks(projectId);
        },
        error: (err) => { console.error(err); this.creating.set(false); },
      });
  }

  getStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    const map: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary'> = {
      New: 'secondary', Active: 'info', Resolved: 'success', Closed: 'warn',
    };
    return map[status] ?? 'secondary';
  }

  getTypeIcon(type: string): string {
    const map: Record<string, string> = {
      Task:      'pi pi-file',
      Bug:       'pi pi-exclamation-circle',
      Epic:      'pi pi-crown',
      Feature:   'pi pi-star',
      UserStory: 'pi pi-user',
      Subtask:   'pi pi-sitemap',
    };
    return map[type] ?? 'pi pi-circle';
  }

  getTypeColor(type: string): string {
    const map: Record<string, string> = {
      Task:      '#eab308',
      Bug:       '#ef4444',
      Epic:      '#a855f7',
      Feature:   '#3b82f6',
      UserStory: '#22c55e',
      Subtask:   '#71717a',
    };
    return map[type] ?? '#71717a';
  }
}
