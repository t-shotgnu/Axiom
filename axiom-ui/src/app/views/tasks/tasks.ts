import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WorkItemService, WorkItem, CreateWorkItemCommand } from '../../core/services/work-item.service';
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
  authorId: string = '00000000-0000-0000-0000-000000000000';
  fetchProjectId: string = '00000000-0000-0000-0000-000000000000';

  newTask: Partial<CreateWorkItemCommand> = {
    controlNo: 1,
    description: '',
    priority: 1,
    type: 'Task',
    status: 'New',
  };

  typeOptions = [
    { label: 'Task', value: 'Task' },
    { label: 'Bug', value: 'Bug' },
    { label: 'Epic', value: 'Epic' },
  ];

  statusOptions = [
    { label: 'New', value: 'New' },
    { label: 'Active', value: 'Active' },
    { label: 'Resolved', value: 'Resolved' },
    { label: 'Closed', value: 'Closed' },
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
    if (!this.projectId || !this.authorId) return;
    const command: CreateWorkItemCommand = {
      ...(this.newTask as CreateWorkItemCommand),
      projectId: this.projectId,
      authorId: this.authorId,
    };

    this.workItemService.createWorkItem(command).subscribe({
      next: () => {
        this.newTask.description = '';
        this.newTask.controlNo = (this.newTask.controlNo || 0) + 1;
        this.fetchProjectId = this.projectId;
        this.loadTasks();
      },
      error: (err) => console.error(err),
    });
  }
}
