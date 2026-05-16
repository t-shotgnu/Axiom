import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, RegisterUserCommand } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="flex items-center justify-center min-h-screen bg-surface-container-lowest">
      <div class="max-w-md w-full bg-surface-container-low p-xl rounded-xl shadow-sm border border-outline-variant">
        <h1 class="font-headline-lg text-headline-lg mb-xl text-center text-on-surface">Create an Account</h1>
        
        <div *ngIf="error" class="bg-error-container text-on-error-container font-body-sm p-sm rounded border border-error mb-lg">
          {{ error }}
        </div>
        
        <form class="space-y-lg" (ngSubmit)="register()">
          <div>
            <label class="block font-label-md mb-xs text-on-surface">Username</label>
            <input type="text" name="userName" [(ngModel)]="command.userName" class="w-full px-md py-sm border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface text-on-surface" required />
          </div>
          <div>
            <label class="block font-label-md mb-xs text-on-surface">Email</label>
            <input type="email" name="email" [(ngModel)]="command.emailAddress" class="w-full px-md py-sm border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface text-on-surface" required />
          </div>
          <div>
            <label class="block font-label-md mb-xs text-on-surface">Password</label>
            <input type="password" name="password" [(ngModel)]="command.password" class="w-full px-md py-sm border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface text-on-surface" required />
          </div>
          <div class="pt-sm">
            <button type="submit" class="w-full bg-primary text-on-primary font-label-md py-sm rounded hover:opacity-90 transition-opacity flex items-center justify-center gap-xs" [disabled]="loading">
              <span *ngIf="loading" class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
              Register
            </button>
          </div>
        </form>
        
        <div class="mt-xl text-center font-body-sm text-on-surface-variant">
          Already have an account? <a routerLink="/login" class="text-primary hover:underline">Sign in here</a>
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
