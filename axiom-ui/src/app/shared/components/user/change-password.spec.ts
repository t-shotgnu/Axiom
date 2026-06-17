import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ChangePasswordComponent } from './change-password';

describe('ChangePasswordComponent', () => {
  let fixture: ComponentFixture<ChangePasswordComponent>;
  let component: ChangePasswordComponent;
  let authService: { changePassword: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    vi.useFakeTimers();
    authService = {
      changePassword: vi.fn(() => of(void 0)),
    };

    await TestBed.configureTestingModule({
      imports: [ChangePasswordComponent],
      providers: [{ provide: AuthService, useValue: authService }],
    }).compileComponents();

    fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('scores password strength and registration requirements', () => {
    (component as any).form.newPassword = '';
    expect(component.passwordStrengthPercentage).toBe(0);
    expect(component.passwordStrengthText).toBe('');
    expect(component.passwordMeetsRegistrationRules).toBe(false);

    (component as any).form.newPassword = 'password';
    expect(component.passwordStrengthPercentage).toBe(25);
    expect(component.passwordStrengthText).toBe('Weak');
    expect(component.passwordMeetsRegistrationRules).toBe(false);

    (component as any).form.newPassword = 'Password1';
    expect(component.passwordStrengthPercentage).toBe(75);
    expect(component.passwordStrengthText).toBe('Good');
    expect(component.passwordMeetsRegistrationRules).toBe(true);

    (component as any).form.newPassword = 'Password1234';
    expect(component.passwordStrengthPercentage).toBe(100);
    expect(component.passwordStrengthText).toBe('Strong');
  });

  it('detects mismatched and repeated passwords', () => {
    (component as any).form = {
      oldPassword: 'OldSecret123',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'Different123',
    };

    expect(component.passwordMismatch).toBe(true);
    expect(component.samePassword).toBe(false);

    (component as any).form.newPassword = 'OldSecret123';
    (component as any).form.newPasswordConfirmation = 'OldSecret123';

    expect(component.passwordMismatch).toBe(false);
    expect(component.samePassword).toBe(true);
  });

  it('does not submit while invalid or already loading', () => {
    (component as any).form = {
      oldPassword: 'OldSecret123',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'Different123',
    };
    component.submit();

    (component as any).form = {
      oldPassword: 'OldSecret123',
      newPassword: 'OldSecret123',
      newPasswordConfirmation: 'OldSecret123',
    };
    component.submit();

    (component as any).form = {
      oldPassword: 'OldSecret123',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'NewSecret123',
    };
    (component as any).loading = true;
    component.submit();

    expect(authService.changePassword).not.toHaveBeenCalled();
  });

  it('submits, shows success, resets the form, and closes after a delay', () => {
    const request$ = new Subject<void>();
    authService.changePassword.mockReturnValue(request$.asObservable());
    const displayChanges: boolean[] = [];
    let closed = false;
    component.display = true;
    component.displayChange.subscribe((value) => displayChanges.push(value));
    component.closed.subscribe(() => {
      closed = true;
    });
    (component as any).form = {
      oldPassword: 'OldSecret123',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'NewSecret123',
    };

    component.submit();

    expect((component as any).loading).toBe(true);
    expect(authService.changePassword).toHaveBeenCalledWith({
      oldPassword: 'OldSecret123',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'NewSecret123',
    });

    request$.next();
    request$.complete();

    expect((component as any).loading).toBe(false);
    expect((component as any).successMessage).toBe('Password changed successfully.');
    expect((component as any).form).toEqual({
      oldPassword: '',
      newPassword: '',
      newPasswordConfirmation: '',
    });

    vi.advanceTimersByTime(1500);

    expect(component.display).toBe(false);
    expect(displayChanges).toEqual([false]);
    expect(closed).toBe(true);
    expect((component as any).successMessage).toBe('');
  });

  it('maps API and fallback errors', () => {
    authService.changePassword.mockReturnValueOnce(
      throwError(() => ({ error: { detail: 'Old password is incorrect.' } })),
    );
    (component as any).form = {
      oldPassword: 'bad',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'NewSecret123',
    };

    component.submit();

    expect((component as any).loading).toBe(false);
    expect((component as any).errorMessage).toBe('Old password is incorrect.');

    authService.changePassword.mockReturnValueOnce(throwError(() => ({ message: 'Network failed' })));
    component.submit();

    expect((component as any).errorMessage).toBe('Network failed');

    authService.changePassword.mockReturnValueOnce(throwError(() => ({})));
    component.submit();

    expect((component as any).errorMessage).toBe('Unable to change password. Please try again.');
  });

  it('close resets sensitive state and emits close events', () => {
    const displayChanges: boolean[] = [];
    let closed = false;
    component.display = true;
    component.displayChange.subscribe((value) => displayChanges.push(value));
    component.closed.subscribe(() => {
      closed = true;
    });
    (component as any).form = {
      oldPassword: 'OldSecret123',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'Different123',
    };
    (component as any).errorMessage = 'Error';
    (component as any).successMessage = 'Success';
    (component as any).loading = true;
    (component as any).showOld = true;
    (component as any).showNew = true;
    (component as any).showConfirm = true;

    component.close();

    expect(component.display).toBe(false);
    expect(displayChanges).toEqual([false]);
    expect(closed).toBe(true);
    expect((component as any).form).toEqual({
      oldPassword: '',
      newPassword: '',
      newPasswordConfirmation: '',
    });
    expect((component as any).errorMessage).toBe('');
    expect((component as any).successMessage).toBe('');
    expect((component as any).loading).toBe(false);
    expect((component as any).showOld).toBe(false);
    expect((component as any).showNew).toBe(false);
    expect((component as any).showConfirm).toBe(false);
  });
});
