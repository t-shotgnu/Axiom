import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService, LoginCommand, RegisterUserCommand } from '../../core/services/auth.service';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, ButtonModule, CardModule],
  templateUrl: './login.html',
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
    private readonly authService: AuthService,
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
        ? this.authService.login(this.toLoginCommand())
        : this.authService.register(this.toRegisterCommand());

    request.pipe(finalize(() => (this.submitting = false))).subscribe({
      next: () => {
        void this.router.navigateByUrl('/projects');
      },
      error: (err: unknown) => {
        this.errorMessage = this.mapAuthError(err);
      },
    });
  }

  toggleMode(): void {
    this.mode = this.mode === 'login' ? 'register' : 'login';
    this.errorMessage = '';
  }

  private mapAuthError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (this.mode === 'login') {
        if (err.status === 401 || err.status === 403) {
          return 'Invalid email or password.';
        }
      } else {
        if (err.status === 400 || err.status === 409) {
          return 'Could not create account. The email or username may already be in use, or the data is invalid.';
        }
      }
    }
    return this.mode === 'login'
      ? 'Could not sign in. Check email and password.'
      : 'Could not create account. Check the provided data.';
  }

  private toLoginCommand(): LoginCommand {
    return {
      emailAddress: this.emailAddress.trim(),
      password: this.password,
    };
  }

  private toRegisterCommand(): RegisterUserCommand {
    return {
      userName: this.registerForm.userName.trim(),
      emailAddress: this.emailAddress.trim(),
      password: this.password,
    };
  }
}
