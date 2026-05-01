import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule],
  template: `
    <div class="grid gap-4">
      <h1 class="text-2xl font-semibold mb-2">Dashboard</h1>
      <div class="grid md:grid-cols-3 gap-4">
        <p-card header="My Tasks">
          <p>Open: 12</p>
        </p-card>
        <p-card header="Team Tasks">
          <p>Open: 48</p>
        </p-card>
        <p-card header="Active Projects">
          <p>5</p>
        </p-card>
      </div>

      <section class="mt-6">
        <h2 class="text-lg font-medium mb-2">Recent activity</h2>
        <div class="space-y-2">
          <div class="bg-white p-3 rounded shadow-sm">Alice commented on Task #23</div>
          <div class="bg-white p-3 rounded shadow-sm">Bob updated Project "Website Redesign"</div>
        </div>
      </section>
    </div>
  `,
  styles: [],
})
export class DashboardComponent {}
