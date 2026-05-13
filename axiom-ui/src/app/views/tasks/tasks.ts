import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  WorkItemService,
  WorkItem,
  CreateWorkItemCommand,
} from '../../core/services/work-item.service';
import { TaskItemComponent } from '../../shared/components/task-item/task-item';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    TaskItemComponent,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ButtonModule,
    CardModule,
  ],
  templateUrl: './tasks.html',
})
export class TasksComponent {
  tasks: WorkItem[] = [];

  projectId: string = '00000000-0000-0000-0000-000000000000';
  fetchProjectId: string = '00000000-0000-0000-0000-000000000000';

  newTask: Partial<CreateWorkItemCommand> = {
    description: '',
    priority: 1,
    type: 'Task',
    status: 'New',
  };

  typeOptions = [
    { label: 'Task', value: 'Task', icon: 'pi pi-file', color: '#eab308' },
    { label: 'Bug', value: 'Bug', icon: 'pi pi-exclamation-circle', color: '#ef4444' },
    { label: 'Epic', value: 'Epic', icon: 'pi pi-crown', color: '#a855f7' },
  ];

  statusOptions = [
    { label: 'New', value: 'New', icon: 'pi pi-circle', color: '#eab308' },
    { label: 'Active', value: 'Active', icon: 'pi pi-circle', color: '#ef4444' },
    { label: 'Resolved', value: 'Resolved', icon: 'pi pi-circle', color: '#a855f7' },
    { label: 'Closed', value: 'Closed', icon: 'pi pi-circle', color: '#a855f7' },
  ];

  constructor(private workItemService: WorkItemService) {}

  loadTasks() {
    if (!this.fetchProjectId) return;
    this.workItemService.getWorkItems(this.fetchProjectId).subscribe({
      next: (data) => (this.tasks = data),
      error: (err) => console.error(err),
    });
  }

  createTask() {
    if (!this.projectId) return;
    const command: CreateWorkItemCommand = {
      ...(this.newTask as CreateWorkItemCommand),
      projectId: this.projectId,
    };

    this.workItemService.createWorkItem(command).subscribe({
      next: () => {
        this.newTask.description = '';
        this.fetchProjectId = this.projectId;
        this.loadTasks();
      },
      error: (err) => console.error(err),
    });
  }
}
