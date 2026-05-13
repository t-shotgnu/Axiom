import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { Router } from '@angular/router';
import { AuthApi } from '../services/auth-api';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MenubarModule, ButtonModule],
  template: `
    <div class="min-h-screen flex flex-col">
      <header class="bg-white border-b">
        <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <a routerLink="/" class="text-xl font-semibold">Axiom</a>
            <nav>
              <a routerLink="/dashboard" class="text-sm px-3">Dashboard</a>
              <a routerLink="/projects" class="text-sm px-3">Projects</a>
              <a routerLink="/tasks" class="text-sm px-3">Tasks</a>
            </nav>
          </div>
          <div class="flex items-center gap-2">
            <a routerLink="/tasks" pButton type="button" label="New Task" class="p-button-outlined"></a>
            @if (authApi.isAuthenticated) {
              <button pButton type="button" label="Logout" class="p-button-text" (click)="logout()"></button>
            } @else {
              <a routerLink="/login" class="text-sm">Sign in</a>
            }
          </div>
        </div>
      </header>

      <div class="flex-1 bg-gray-50">
        <main class="max-w-7xl mx-auto p-4">
          <router-outlet></router-outlet>
        </main>
      </div>

      <footer class="border-t text-sm bg-white">
        <div class="max-w-7xl mx-auto p-3 text-center">© Axiom — Task management</div>
      </footer>
    </div>
  `,
  styles: [],
})
export class AppLayoutComponent {
  constructor(
    protected readonly authApi: AuthApi,
    private readonly router: Router,
  ) {}

  logout(): void {
    this.authApi.logout();
    void this.router.navigateByUrl('/login');
  }
}
