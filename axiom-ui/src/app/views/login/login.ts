import { ChangeDetectorRef, Component, inject } from '@angular/core';
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
  private readonly cdr = inject(ChangeDetectorRef);

  mode: AuthMode = 'login';
  submitting = false;
  errorMessage = '';
  emailAddress = '';
  password = '';
  showPassword = false;
  registerForm = {
    userName: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
  };

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) { }

  get canSubmit(): boolean {
    const baseFieldsReady = this.isEmailValid(this.emailAddress) && this.password.trim() !== '';

    if (this.mode === 'login') {
      return baseFieldsReady;
    }

    return (
      baseFieldsReady &&
      this.isUserNameValid(this.registerForm.userName) &&
      this.isNameValid(this.registerForm.firstName) &&
      this.isNameValid(this.registerForm.lastName) &&
      this.isDateOfBirthValid(this.registerForm.dateOfBirth) &&
      this.passwordMeetsRegistrationRules
    );
  }

  get todayDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
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
    if (!this.password) return 'None';
    const pct = this.passwordStrengthPercentage;
    if (pct >= 100) return 'Strong';
    if (pct >= 50) return 'Medium';
    return 'Weak';
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

    request.pipe(finalize(() => {
      this.submitting = false;
      this.cdr.detectChanges();
    })).subscribe({
      next: () => {
        void this.router.navigateByUrl('/projects');
      },
      error: (err: unknown) => {
        this.errorMessage = this.mapAuthError(err);
        this.cdr.detectChanges();
      },
    });
  }

  toggleMode(): void {
    this.mode = this.mode === 'login' ? 'register' : 'login';
    this.errorMessage = '';
    this.submitting = false;
    this.emailAddress = '';
    this.password = '';
    this.showPassword = false;
    this.resetRegisterForm();
  }

  mapAuthError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (this.mode === 'login') {
        if (err.status === 401 || err.status === 403) {
          return 'Invalid email or password.';
        }
      } else {
        if (err.status === 400 || err.status === 409) {
          return 'Cannot create account. This email or username may already be in use.';
        }
      }
    }
    return this.mode === 'login'
      ? 'Cannot log in. Please check your email and password.'
      : 'Cannot create account. Please check the entered data.';
  }

  toLoginCommand(): LoginCommand {
    return {
      emailAddress: this.emailAddress.trim(),
      password: this.password,
    };
  }

  toRegisterCommand(): RegisterUserCommand {
    return {
      userName: this.registerForm.userName.trim(),
      firstName: this.registerForm.firstName.trim(),
      lastName: this.registerForm.lastName.trim(),
      dateOfBirth: this.registerForm.dateOfBirth,
      emailAddress: this.emailAddress.trim(),
      password: this.password,
    };
  }

  resetRegisterForm(): void {
    this.registerForm = {
      userName: '',
      firstName: '',
      lastName: '',
      dateOfBirth: '',
    };
  }

  isEmailValid(emailAddress: string): boolean {
    const value = emailAddress.trim();
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }

  isUserNameValid(userName: string): boolean {
    const value = userName.trim();
    return value.length >= 3 && value.length <= 30;
  }

  isNameValid(name: string): boolean {
    const value = name.trim();
    return /^[A-Za-zÀ-ž' -]{2,40}$/.test(value);
  }

  isDateOfBirthValid(dateOfBirth: string): boolean {
    if (dateOfBirth.trim() === '') {
      return false;
    }

    const inputDate = new Date(`${dateOfBirth}T00:00:00`);
    if (Number.isNaN(inputDate.getTime())) {
      return false;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return inputDate <= today;
  }
}
