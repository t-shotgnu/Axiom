import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MenubarModule, ButtonModule],
  template: `
    <div class="min-h-screen flex flex-col bg-gray-50 dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100">
      <header class="bg-white dark:bg-zinc-900 border-b border-zinc-200 dark:border-zinc-800">
        <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <a routerLink="/" class="text-xl font-semibold text-zinc-900 dark:text-zinc-100">Axiom</a>
            <nav class="flex gap-2">
              <a routerLink="/dashboard" class="text-sm px-3 hover:text-blue-500 transition-colors">Dashboard</a>
              <a routerLink="/projects" class="text-sm px-3 hover:text-blue-500 transition-colors">Projects</a>
              <a routerLink="/tasks" class="text-sm px-3 hover:text-blue-500 transition-colors">Tasks</a>
            </nav>
          </div>
          <div class="flex items-center gap-2">
            <a routerLink="/tasks" pButton type="button" label="New Task" class="p-button-outlined"></a>
            <a routerLink="/login" class="text-sm px-3 hover:text-blue-500 transition-colors">Profile</a>
          </div>
        </div>
      </header>

      <div class="flex-1">
        <main class="max-w-7xl mx-auto p-4">
          <router-outlet></router-outlet>
        </main>
      </div>

      <footer class="border-t border-zinc-200 dark:border-zinc-800 text-sm bg-white dark:bg-zinc-900">
        <div class="max-w-7xl mx-auto p-3 text-center text-zinc-500 dark:text-zinc-400">© Axiom — Task management</div>
      </footer>
    </div>
  `,
  styles: [],
})
export class AppLayoutComponent {}
