import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <h1 class="text-2xl font-semibold mb-2">Task Detail</h1>
      <div class="bg-white p-4 rounded shadow-sm">
        <p class="text-sm text-gray-600">Task ID: {{ id }}</p>
        <h2 class="font-medium mt-2">Placeholder task title</h2>
        <p class="mt-2">Description and comments will go here.</p>
      </div>
    </div>
  `,
})
export class TaskDetailComponent {
  id: string | null = null;
  constructor(private route: ActivatedRoute) {
    this.id = this.route.snapshot.paramMap.get('id');
  }
}
