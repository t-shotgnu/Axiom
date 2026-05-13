import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { Router, RouterModule } from '@angular/router';
import { AuthService, RegisterUserCommand } from '../../core/services/auth.service';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, ButtonModule, RouterModule, MessageModule],
  template: `
    <div class="flex items-center justify-center min-h-screen bg-zinc-50 dark:bg-zinc-900">
      <div class="max-w-md w-full bg-white dark:bg-zinc-950 p-8 rounded-xl shadow-sm border border-zinc-200 dark:border-zinc-800">
        <h1 class="text-2xl font-semibold mb-6 text-center text-zinc-900 dark:text-white">Create an Account</h1>
        
        <p-message *ngIf="error" severity="error" [text]="error" styleClass="w-full mb-4"></p-message>
        
        <form class="space-y-4" (ngSubmit)="register()">
          <div>
            <label class="block text-sm mb-1 font-medium text-zinc-700 dark:text-zinc-300">Username</label>
            <input pInputText type="text" name="userName" [(ngModel)]="command.userName" class="w-full" required />
          </div>
          <div>
            <label class="block text-sm mb-1 font-medium text-zinc-700 dark:text-zinc-300">Email</label>
            <input pInputText type="email" name="email" [(ngModel)]="command.emailAddress" class="w-full" required />
          </div>
          <div>
            <label class="block text-sm mb-1 font-medium text-zinc-700 dark:text-zinc-300">Password</label>
            <input pInputText type="password" name="password" [(ngModel)]="command.password" class="w-full" required />
          </div>
          <div class="pt-2">
            <button pButton type="submit" label="Register" class="w-full" [loading]="loading"></button>
          </div>
        </form>
        
        <div class="mt-6 text-center text-sm text-zinc-500 dark:text-zinc-400">
          Already have an account? <a routerLink="/login" class="text-blue-600 dark:text-blue-400 hover:underline">Sign in here</a>
        </div>
      </div>
    </div>
  `,
})
export class RegisterComponent {
  command: RegisterUserCommand = { userName: '', emailAddress: '', password: '' };
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    this.error = '';
    this.loading = true;
    this.authService.register(this.command).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Registration failed. Check inputs.';
        console.error(err);
      }
    });
  }
}
