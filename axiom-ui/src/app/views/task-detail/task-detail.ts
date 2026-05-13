import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WorkItemService, WorkItem } from '../../core/services/work-item.service';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, SelectModule, ButtonModule, CardModule, TagModule],
  template: `
    <div *ngIf="task" class="text-zinc-900 dark:text-zinc-100 max-w-4xl mx-auto">
      <h1 class="text-2xl font-semibold mb-4">Task Detail</h1>
      
      <p-card styleClass="mb-4 shadow-sm">
        <ng-template pTemplate="title">
          <div class="flex justify-between items-center">
            <span class="text-xl">#{{ task.controlNo }} - {{ task.description }}</span>
            <p-tag [value]="task.status" [severity]="getSeverity(task.status)"></p-tag>
          </div>
        </ng-template>
        <ng-template pTemplate="subtitle">
          <span class="text-sm">Task ID: {{ task.id }}</span>
        </ng-template>

        <div class="grid grid-cols-2 gap-4 mt-2 text-sm">
          <div><span class="font-bold">Project ID:</span> {{ task.projectId }}</div>
          <div><span class="font-bold">Author ID:</span> {{ task.authorId }}</div>
          <div><span class="font-bold">Priority:</span> {{ task.priority }}</div>
          <div><span class="font-bold">Type:</span> {{ task.type }}</div>
        </div>

        <hr class="my-6 border-zinc-200 dark:border-zinc-700" />

        <div class="grid md:grid-cols-2 gap-8">
          <div>
            <h3 class="font-medium mb-3">Update Status</h3>
            <div class="flex gap-2">
              <p-select [options]="statusOptions" [(ngModel)]="status" placeholder="Select Status" [appendTo]="'body'"></p-select>
              <p-button label="Save Status" icon="pi pi-check" severity="success" (onClick)="updateStatus()"></p-button>
            </div>
          </div>

          <div>
            <h3 class="font-medium mb-3">Assign User</h3>
            <div class="flex gap-2">
              <input pInputText [(ngModel)]="assigneeId" placeholder="User ID (UUID)" class="w-full max-w-xs" />
              <p-button label="Assign" icon="pi pi-user-plus" severity="info" (onClick)="assignUser()"></p-button>
            </div>
            <div class="mt-3 text-sm">
              <span class="font-bold">Current Assignee:</span> 
              <span class="ml-2 px-2 py-1 bg-zinc-100 dark:bg-zinc-800 rounded">{{ task.assigneeId || 'Unassigned' }}</span>
            </div>
          </div>
        </div>
      </p-card>
    </div>
    <div *ngIf="!task" class="text-zinc-900 dark:text-zinc-100">Loading...</div>
  `,
})
export class TaskDetailComponent implements OnInit {
  id: string | null = null;
  task: WorkItem | null = null;
  status: string = '';
  assigneeId: string = '';

  statusOptions = [
    { label: 'New', value: 'New' },
    { label: 'Active', value: 'Active' },
    { label: 'Resolved', value: 'Resolved' },
    { label: 'Closed', value: 'Closed' }
  ];

  constructor(
    private route: ActivatedRoute,
    private workItemService: WorkItemService,
  ) {
    this.id = this.route.snapshot.paramMap.get('id');
  }

  ngOnInit() {
    this.loadTask();
  }

  loadTask() {
    if (this.id) {
      this.workItemService.getWorkItemById(this.id).subscribe({
        next: (data) => {
          this.task = data;
          this.status = data.status;
          this.assigneeId = data.assigneeId || '';
        },
        error: (err) => console.error(err),
      });
    }
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
    if (this.id && this.assigneeId) {
      this.workItemService.assignWorkItem(this.id, { assigneeId: this.assigneeId }).subscribe({
        next: () => this.loadTask(),
        error: (err) => console.error(err),
      });
    }
  }

  getSeverity(status: string): "success" | "secondary" | "info" | "warn" | "danger" | "contrast" | undefined {
    switch (status) {
      case 'New': return 'info';
      case 'Active': return 'warn';
      case 'Resolved': return 'success';
      case 'Closed': return 'secondary';
      default: return 'info';
    }
  }
}
