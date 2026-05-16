import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService, LoginCommand, RegisterUserCommand } from '../../core/services/auth.service';
import { ButtonComponent } from '../../shared/components/ui/button';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonComponent],
  templateUrl: './login.html',
})
export class LoginComponent {
  mode: AuthMode = 'login';
  submitting = false;
  errorMessage = '';
  emailAddress = '';
  password = '';
  showPassword = false;
  rememberMe = false;
  agreeTerms = false;
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
      this.passwordMeetsRegistrationRules &&
      this.agreeTerms
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

  get passwordStrengthText(): string {
    if (!this.password) return 'Brak';
    const pct = this.passwordStrengthPercentage;
    if (pct >= 100) return 'Mocne';
    if (pct >= 50) return 'Średnie';
    return 'Słabe';
  }

  get passwordStrengthPercentage(): number {
    if (!this.password) return 0;
    let score = 0;
    if (this.passwordLongEnough) score += 25;
    if (this.passwordHasUppercase) score += 25;
    if (this.passwordHasLowercase) score += 25;
    if (this.passwordHasNumber) score += 25;
    return score;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
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
          return 'Nieprawidłowy e-mail lub hasło.';
        }
      } else {
        if (err.status === 400 || err.status === 409) {
          return 'Nie można utworzyć konta. Ten e-mail lub nazwa użytkownika mogą być już w użyciu.';
        }
      }
    }
    return this.mode === 'login'
      ? 'Nie można się zalogować. Sprawdź e-mail i hasło.'
      : 'Nie można utworzyć konta. Sprawdź wprowadzone dane.';
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
