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
  templateUrl: './task-detail.html',
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
