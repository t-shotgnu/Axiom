import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WorkItem } from '../../../core/services/work-item.service';


@Component({
  selector: 'app-task-item',
  standalone: true,
  imports: [CommonModule, RouterModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a [routerLink]="linkEnabled ? ['/tasks', task.id] : null" 
       class="block p-4 rounded-lg shadow-sm border border-outline-variant bg-surface-container-lowest transition-shadow text-on-surface"
       [class.hover:shadow-md]="linkEnabled"
       [class.cursor-pointer]="linkEnabled">
      <div class="flex justify-between items-start mb-2">
        <span class="font-medium text-lg">#{{ task.controlNo }} — {{ task.description }}</span>
        <span class="bg-surface-container-high text-on-surface px-sm py-1 rounded text-label-sm uppercase font-bold">{{ task.status }}</span>
      </div>
      <div class="text-sm text-on-surface-variant">
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
