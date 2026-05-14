import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WorkItem } from '../../../core/services/work-item.service';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-task-item',
  standalone: true,
  imports: [CommonModule, RouterModule, TagModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a [routerLink]="linkEnabled ? ['/tasks', task.id] : null" 
       class="block p-4 rounded-lg shadow-sm border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 transition-shadow text-zinc-900 dark:text-zinc-100"
       [class.hover:shadow-md]="linkEnabled"
       [class.cursor-pointer]="linkEnabled">
      <div class="flex justify-between items-start mb-2">
        <span class="font-medium text-lg">#{{ task.controlNo }} — {{ task.description }}</span>
        <p-tag [value]="task.status" [severity]="getSeverity(task.status)"></p-tag>
      </div>
      <div class="text-sm text-zinc-500 dark:text-zinc-400">
        Project: {{ task.projectId }} | Priority: {{ task.priority }} | Type: {{ task.type }}
      </div>
    </a>
  `
})
export class TaskItemComponent {
  @Input({ required: true }) task!: WorkItem;
  @Input() linkEnabled: boolean = true;

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
