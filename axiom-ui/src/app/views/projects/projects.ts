import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <h1 class="text-2xl font-semibold mb-4">Projects</h1>
      <div class="grid gap-3">
        <div class="bg-white p-4 rounded shadow-sm">Project: Website Redesign — 12 tasks</div>
        <div class="bg-white p-4 rounded shadow-sm">Project: Mobile App — 8 tasks</div>
        <div class="bg-white p-4 rounded shadow-sm">Project: Internal Tools — 5 tasks</div>
      </div>
    </div>
  `,
})
export class ProjectsComponent {}
