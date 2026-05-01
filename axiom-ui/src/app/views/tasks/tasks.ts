import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div>
      <h1 class="text-2xl font-semibold mb-4">Tasks</h1>
      <div class="space-y-2">
        <a routerLink="/tasks/1" class="block bg-white p-3 rounded shadow-sm">#1 — Fix header layout</a>
        <a routerLink="/tasks/2" class="block bg-white p-3 rounded shadow-sm">#2 — Update login flow</a>
        <a routerLink="/tasks/3" class="block bg-white p-3 rounded shadow-sm">#3 — Add file uploads</a>
      </div>
    </div>
  `,
})
export class TasksComponent {}
