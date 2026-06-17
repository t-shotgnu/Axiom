import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('posts login credentials and stores the returned token', () => {
    const authStates: boolean[] = [];
    service.authStatus$.subscribe((state) => authStates.push(state));

    service.login({ emailAddress: 'user@example.com', password: 'secret' }).subscribe((response) => {
      expect(response).toEqual({ token: 'jwt-token', refreshToken: 'refresh-token' });
    });

    const request = httpMock.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ emailAddress: 'user@example.com', password: 'secret' });

    request.flush({ token: 'jwt-token', refreshToken: 'refresh-token' });

    expect(localStorage.getItem('axiom_jwt_token')).toBe('jwt-token');
    expect(localStorage.getItem('axiom_refresh_token')).toBe('refresh-token');
    expect(service.getToken()).toBe('jwt-token');
    expect(service.getRefreshToken()).toBe('refresh-token');
    expect(service.hasToken()).toBe(true);
    expect(authStates).toEqual([false, true]);
  });

  it('posts registration data and stores the returned token', () => {
    service
      .register({
        userName: 'tester',
        emailAddress: 'tester@example.com',
        password: 'Secret123',
        firstName: 'Test',
        lastName: 'User',
        dateOfBirth: '1990-05-20',
      })
      .subscribe();

    const request = httpMock.expectOne('/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      userName: 'tester',
      emailAddress: 'tester@example.com',
      password: 'Secret123',
      firstName: 'Test',
      lastName: 'User',
      dateOfBirth: '1990-05-20',
    });

    request.flush({ token: 'register-token', refreshToken: 'register-refresh-token' });

    expect(service.getToken()).toBe('register-token');
    expect(service.getRefreshToken()).toBe('register-refresh-token');
    expect(service.hasToken()).toBe(true);
  });

  it('refreshes tokens using the stored refresh token', () => {
    localStorage.setItem('axiom_refresh_token', 'stored-refresh-token');

    service.refresh().subscribe((response) => {
      expect(response).toEqual({ token: 'new-jwt', refreshToken: 'new-refresh' });
    });

    const request = httpMock.expectOne('/api/auth/refresh-token');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ refreshToken: 'stored-refresh-token' });

    request.flush({ token: 'new-jwt', refreshToken: 'new-refresh' });

    expect(service.getToken()).toBe('new-jwt');
    expect(service.getRefreshToken()).toBe('new-refresh');
  });

  it('throws when refreshing without a stored refresh token', () => {
    expect(() => service.refresh()).toThrow('No refresh token available');
    httpMock.expectNone('/api/auth/refresh-token');
  });

  it('posts password change data without changing stored tokens', () => {
    localStorage.setItem('axiom_jwt_token', 'existing-token');
    localStorage.setItem('axiom_refresh_token', 'existing-refresh-token');
    const command = {
      oldPassword: 'old-secret',
      newPassword: 'NewSecret123',
      newPasswordConfirmation: 'NewSecret123',
    };

    service.changePassword(command).subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/auth/change-password');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(command);
    request.flush(null);

    expect(service.getToken()).toBe('existing-token');
    expect(service.getRefreshToken()).toBe('existing-refresh-token');
  });

  it('initializes as authenticated when a token is already stored', () => {
    localStorage.setItem('axiom_jwt_token', 'existing-token');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    const freshService = TestBed.inject(AuthService);
    const freshHttpMock = TestBed.inject(HttpTestingController);
    const authStates: boolean[] = [];
    freshService.authStatus$.subscribe((state) => authStates.push(state));

    expect(freshService.hasToken()).toBe(true);
    expect(authStates).toEqual([true]);

    freshHttpMock.verify();
  });

  it('removes the token and publishes an unauthenticated state on logout', () => {
    localStorage.setItem('axiom_jwt_token', 'existing-token');
    localStorage.setItem('axiom_refresh_token', 'existing-refresh-token');
    const authStates: boolean[] = [];
    service.authStatus$.subscribe((state) => authStates.push(state));

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.getRefreshToken()).toBeNull();
    expect(service.hasToken()).toBe(false);
    expect(authStates.at(-1)).toBe(false);
  });
});
