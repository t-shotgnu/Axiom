import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthApi, LoginRequest, RegisterRequest } from '../../services/auth-api';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, ButtonModule, CardModule],
  template: `
    <div class="max-w-md mx-auto mt-12 text-zinc-900 dark:text-zinc-100">
      <p-card styleClass="shadow-sm border border-zinc-200 dark:border-zinc-700">
        <div class="mb-5">
          <p class="text-sm uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Axiom</p>
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
                class="w-full"
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

          @if (mode === 'register') {
            <div class="grid gap-1 text-xs text-zinc-500 dark:text-zinc-400">
              <span [class.text-green-600]="passwordLongEnough">At least 8 characters</span>
              <span [class.text-green-600]="passwordHasUppercase && passwordHasLowercase">
                Uppercase and lowercase letter
              </span>
              <span [class.text-green-600]="passwordHasNumber">At least one number</span>
            </div>
          }

          @if (errorMessage) {
            <div class="rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-300">
              {{ errorMessage }}
            </div>
          }

          <p-button
            type="submit"
            styleClass="w-full"
            [label]="mode === 'login' ? 'Sign in' : 'Create account'"
            [disabled]="submitting || !canSubmit"
          ></p-button>
        </form>

        <p-button
          type="button"
          styleClass="mt-4 w-full"
          severity="secondary"
          [text]="true"
          [label]="
            mode === 'login'
              ? 'Need an account? Register'
              : 'Already have an account? Sign in'
          "
          (onClick)="toggleMode()"
        ></p-button>
      </p-card>
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

    return (
      baseFieldsReady &&
      this.registerForm.userName.trim() !== '' &&
      this.passwordMeetsRegistrationRules
    );
  }

  get passwordLongEnough(): boolean {
    return this.password.length >= 8;
  }

  get passwordHasUppercase(): boolean {
    return /[A-Z]/.test(this.password);
  }

  get passwordHasLowercase(): boolean {
    return /[a-z]/.test(this.password);
  }

  get passwordHasNumber(): boolean {
    return /\d/.test(this.password);
  }

  get passwordMeetsRegistrationRules(): boolean {
    return (
      this.passwordLongEnough &&
      this.passwordHasUppercase &&
      this.passwordHasLowercase &&
      this.passwordHasNumber
    );
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
