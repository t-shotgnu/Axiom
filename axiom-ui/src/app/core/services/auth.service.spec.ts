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
      expect(response).toEqual({ token: 'jwt-token' });
    });

    const request = httpMock.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ emailAddress: 'user@example.com', password: 'secret' });

    request.flush({ token: 'jwt-token' });

    expect(localStorage.getItem('axiom_jwt_token')).toBe('jwt-token');
    expect(service.getToken()).toBe('jwt-token');
    expect(service.hasToken()).toBe(true);
    expect(authStates).toEqual([false, true]);
  });

  it('posts registration data and stores the returned token', () => {
    service
      .register({
        userName: 'tester',
        emailAddress: 'tester@example.com',
        password: 'Secret123',
      })
      .subscribe();

    const request = httpMock.expectOne('/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      userName: 'tester',
      emailAddress: 'tester@example.com',
      password: 'Secret123',
    });

    request.flush({ token: 'register-token' });

    expect(service.getToken()).toBe('register-token');
    expect(service.hasToken()).toBe(true);
  });

  it('removes the token and publishes an unauthenticated state on logout', () => {
    localStorage.setItem('axiom_jwt_token', 'existing-token');
    const authStates: boolean[] = [];
    service.authStatus$.subscribe((state) => authStates.push(state));

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.hasToken()).toBe(false);
    expect(authStates.at(-1)).toBe(false);
  });
});
