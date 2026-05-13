import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthApi, LoginRequest, RegisterRequest } from '../../services/auth-api';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, ButtonModule],
  template: `
    <div class="max-w-md mx-auto mt-12">
      <div class="rounded bg-white p-6 shadow-sm">
        <div class="mb-5">
          <p class="text-sm uppercase tracking-wide text-slate-500">Axiom</p>
          <h1 class="text-2xl font-semibold">
            {{ mode === 'login' ? 'Sign in' : 'Create account' }}
          </h1>
        </div>

        <form class="space-y-4" (ngSubmit)="submit()">
          @if (mode === 'register') {
            <label class="grid gap-1 text-sm">
              Username
              <input
                pInputText
                name="userName"
                required
                [(ngModel)]="registerForm.userName"
                placeholder="dawid"
              />
            </label>
          }

          <label class="grid gap-1 text-sm">
            Email
            <input
              pInputText
              type="email"
              name="email"
              required
              class="w-full"
              [(ngModel)]="emailAddress"
              placeholder="you@example.com"
            />
          </label>

          <label class="grid gap-1 text-sm">
            Password
            <input
              pInputText
              type="password"
              name="password"
              required
              class="w-full"
              [(ngModel)]="password"
              placeholder="Your password"
            />
          </label>

          @if (errorMessage) {
            <div class="rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {{ errorMessage }}
            </div>
          }

          <button
            pButton
            type="submit"
            class="w-full"
            [label]="mode === 'login' ? 'Sign in' : 'Create account'"
            [disabled]="submitting || !canSubmit"
          ></button>
        </form>

        <button
          type="button"
          class="mt-4 w-full text-sm text-slate-600"
          (click)="toggleMode()"
        >
          {{
            mode === 'login'
              ? 'Need an account? Register'
              : 'Already have an account? Sign in'
          }}
        </button>
      </div>
    </div>
  `,
})
export class LoginComponent {
  mode: AuthMode = 'login';
  submitting = false;
  errorMessage = '';
  emailAddress = '';
  password = '';
  registerForm = {
    userName: '',
  };

  constructor(
    private readonly authApi: AuthApi,
    private readonly router: Router,
  ) {}

  get canSubmit(): boolean {
    const baseFieldsReady = this.emailAddress.trim() !== '' && this.password.trim() !== '';

    if (this.mode === 'login') {
      return baseFieldsReady;
    }

    return baseFieldsReady && this.registerForm.userName.trim() !== '';
  }

  submit(): void {
    this.submitting = true;
    this.errorMessage = '';

    const request =
      this.mode === 'login'
        ? this.authApi.login(this.toLoginRequest())
        : this.authApi.register(this.toRegisterRequest());

    request.pipe(finalize(() => (this.submitting = false))).subscribe({
      next: () => {
        void this.router.navigateByUrl('/projects');
      },
      error: () => {
        this.errorMessage =
          this.mode === 'login'
            ? 'Could not sign in. Check email and password.'
            : 'Could not create account. Check the provided data.';
      },
    });
  }

  toggleMode(): void {
    this.mode = this.mode === 'login' ? 'register' : 'login';
    this.errorMessage = '';
  }

  private toLoginRequest(): LoginRequest {
    return {
      emailAddress: this.emailAddress.trim(),
      password: this.password,
    };
  }

  private toRegisterRequest(): RegisterRequest {
    return {
      userName: this.registerForm.userName.trim(),
      emailAddress: this.emailAddress.trim(),
      password: this.password,
    };
  }
}
