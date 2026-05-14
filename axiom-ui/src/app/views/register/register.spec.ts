import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { RegisterComponent } from './register';

describe('RegisterComponent', () => {
  let authService: { register: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let component: RegisterComponent;

  beforeEach(() => {
    authService = {
      register: vi.fn(),
    };
    router = {
      navigate: vi.fn(),
    };
    component = new RegisterComponent(
      authService as unknown as AuthService,
      router as unknown as Router,
    );
  });

  it('registers with the current command and navigates home on success', () => {
    authService.register.mockReturnValue(of({ token: 'jwt-token' }));
    component.command = {
      userName: 'tester',
      emailAddress: 'tester@example.com',
      password: 'Secret123',
    };

    component.register();

    expect(authService.register).toHaveBeenCalledWith(component.command);
    expect(component.loading).toBe(false);
    expect(component.error).toBe('');
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('shows an API error message when registration fails', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    authService.register.mockReturnValue(
      throwError(() => ({ error: { message: 'Email already exists.' } })),
    );

    component.register();

    expect(component.loading).toBe(false);
    expect(component.error).toBe('Email already exists.');
    expect(router.navigate).not.toHaveBeenCalled();
    consoleError.mockRestore();
  });

  it('falls back to a generic error when the API does not return a message', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    authService.register.mockReturnValue(throwError(() => ({ error: {} })));

    component.register();

    expect(component.error).toBe('Registration failed. Check inputs.');
    consoleError.mockRestore();
  });
});
