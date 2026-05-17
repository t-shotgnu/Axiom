import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { LoginComponent } from './login';

describe('LoginComponent', () => {
  let authService: {
    login: ReturnType<typeof vi.fn>;
    register: ReturnType<typeof vi.fn>;
  };
  let router: { navigateByUrl: ReturnType<typeof vi.fn> };
  let detectChanges: ReturnType<typeof vi.fn>;
  let component: LoginComponent;

  beforeEach(() => {
    authService = {
      login: vi.fn(),
      register: vi.fn(),
    };
    router = {
      navigateByUrl: vi.fn(),
    };
    detectChanges = vi.fn();
    component = new LoginComponent(
      authService as unknown as AuthService,
      router as unknown as Router,
    );
    (component as unknown as { cdr: { detectChanges: () => void } }).cdr = { detectChanges };
  });

  it('allows login submit when email and password are present', () => {
    component.emailAddress = ' user@example.com ';
    component.password = 'secret';

    expect(component.canSubmit).toBe(true);
  });

  it('requires username and password rules in register mode', () => {
    component.toggleMode();
    component.emailAddress = 'new@example.com';
    component.registerForm.userName = 'new-user';
    component.registerForm.firstName = 'New';
    component.registerForm.lastName = 'User';
    component.registerForm.dateOfBirth = '1990-05-20';
    component.password = 'weak';

    expect(component.canSubmit).toBe(false);

    component.password = 'Strong123';

    expect(component.passwordLongEnough).toBe(true);
    expect(component.passwordHasUppercase).toBe(true);
    expect(component.passwordHasLowercase).toBe(true);
    expect(component.passwordHasNumber).toBe(true);
    expect(component.canSubmit).toBe(true);
  });

  it('rejects future birth dates in register mode', () => {
    component.toggleMode();
    component.emailAddress = 'new@example.com';
    component.registerForm.userName = 'new-user';
    component.registerForm.firstName = 'New';
    component.registerForm.lastName = 'User';
    component.registerForm.dateOfBirth = '2999-01-01';
    component.password = 'Strong123';

    expect(component.canSubmit).toBe(false);
  });

  it('rejects invalid email in register mode', () => {
    component.toggleMode();
    component.emailAddress = 'not-an-email';
    component.registerForm.userName = 'new-user';
    component.registerForm.firstName = 'New';
    component.registerForm.lastName = 'User';
    component.registerForm.dateOfBirth = '1990-05-20';
    component.password = 'Strong123';

    expect(component.canSubmit).toBe(false);
  });

  it('trims login email and navigates to projects on success', () => {
    authService.login.mockReturnValue(of({ token: 'jwt-token', refreshToken: 'refresh-token' }));
    component.emailAddress = ' user@example.com ';
    component.password = 'secret';

    component.submit();

    expect(authService.login).toHaveBeenCalledWith({
      emailAddress: 'user@example.com',
      password: 'secret',
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/projects');
    expect(component.submitting).toBe(false);
    expect(component.errorMessage).toBe('');
  });

  it('trims register fields and navigates to projects on success', () => {
    authService.register.mockReturnValue(of({ token: 'jwt-token', refreshToken: 'refresh-token' }));
    component.toggleMode();
    component.emailAddress = ' new@example.com ';
    component.registerForm.userName = ' new-user ';
    component.registerForm.firstName = ' New ';
    component.registerForm.lastName = ' User ';
    component.registerForm.dateOfBirth = '1990-05-20';
    component.password = 'Strong123';

    component.submit();

    expect(authService.register).toHaveBeenCalledWith({
      userName: 'new-user',
      firstName: 'New',
      lastName: 'User',
      dateOfBirth: '1990-05-20',
      emailAddress: 'new@example.com',
      password: 'Strong123',
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/projects');
    expect(component.submitting).toBe(false);
  });

  it('shows a specific login error for unauthorized responses', () => {
    authService.login.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401 })),
    );

    component.submit();

    expect(component.errorMessage).toBe('Invalid email or password.');
    expect(component.submitting).toBe(false);
    expect(detectChanges).toHaveBeenCalled();
  });

  it('clears loading state after a login error', () => {
    authService.login.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 400 })),
    );

    component.submit();

    expect(component.submitting).toBe(false);
  });

  it('shows a specific register error for validation or conflict responses', () => {
    authService.register.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409 })),
    );
    component.toggleMode();

    component.submit();

    expect(component.errorMessage).toBe(
      'Could not create account. The email or username may already be in use, or the data is invalid.',
    );
    expect(component.submitting).toBe(false);
  });

  it('clears errors when switching modes', () => {
    component.errorMessage = 'Previous error';
    component.emailAddress = 'user@example.com';
    component.password = 'Strong123';
    component.registerForm.userName = 'new-user';
    component.registerForm.firstName = 'New';
    component.registerForm.lastName = 'User';
    component.registerForm.dateOfBirth = '1990-05-20';

    component.toggleMode();

    expect(component.mode).toBe('register');
    expect(component.errorMessage).toBe('');
    expect(component.emailAddress).toBe('');
    expect(component.password).toBe('');
    expect(component.registerForm).toEqual({
      userName: '',
      firstName: '',
      lastName: '',
      dateOfBirth: '',
    });
  });
});
