import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../core/services/auth.service';
import { authTokenInterceptor } from './auth-token-interceptor';

describe('authTokenInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: {
    getToken: ReturnType<typeof vi.fn>;
    getRefreshToken: ReturnType<typeof vi.fn>;
    refresh: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    authService = {
      getToken: vi.fn(),
      getRefreshToken: vi.fn(),
      refresh: vi.fn(),
      logout: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authTokenInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('adds a bearer token to relative API requests', () => {
    authService.getToken.mockReturnValue('jwt-token');

    http.get('/api/projects').subscribe();

    const request = httpMock.expectOne('/api/projects');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush([]);
  });

  it('adds a bearer token to absolute API requests', () => {
    authService.getToken.mockReturnValue('jwt-token');

    http.get('https://example.com/api/projects').subscribe();

    const request = httpMock.expectOne('https://example.com/api/projects');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush([]);
  });

  it('does not add a token when the user is unauthenticated', () => {
    authService.getToken.mockReturnValue(null);

    http.get('/api/projects').subscribe();

    const request = httpMock.expectOne('/api/projects');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush([]);
  });

  it('does not add a token to non-API requests', () => {
    authService.getToken.mockReturnValue('jwt-token');

    http.get('https://cdn.example.com/assets/logo.svg').subscribe();

    const request = httpMock.expectOne('https://cdn.example.com/assets/logo.svg');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush('');
  });

  it('does not add a token to auth endpoints', () => {
    authService.getToken.mockReturnValue('jwt-token');

    http.post('/api/auth/refresh-token', { refreshToken: 'refresh-token' }).subscribe();

    const request = httpMock.expectOne('/api/auth/refresh-token');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({ token: 'new-jwt', refreshToken: 'new-refresh' });
  });

  it('adds a bearer token to change-password requests', () => {
    authService.getToken.mockReturnValue('jwt-token');

    http.post('/api/auth/change-password', {
      oldPassword: 'old-secret',
      newPassword: 'new-secret',
      newPasswordConfirmation: 'new-secret',
    }).subscribe();

    const request = httpMock.expectOne('/api/auth/change-password');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush({});
  });

  it('refreshes tokens and retries the request after a 401 response', () => {
    authService.getToken.mockReturnValue('expired-jwt');
    authService.getRefreshToken.mockReturnValue('stored-refresh');
    authService.refresh.mockReturnValue(of({ token: 'new-jwt', refreshToken: 'new-refresh' }));

    http.get('/api/projects').subscribe();

    const firstRequest = httpMock.expectOne('/api/projects');
    expect(firstRequest.request.headers.get('Authorization')).toBe('Bearer expired-jwt');
    firstRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authService.refresh).toHaveBeenCalledTimes(1);
    expect(authService.logout).not.toHaveBeenCalled();

    const retriedRequest = httpMock.expectOne('/api/projects');
    expect(retriedRequest.request.headers.get('Authorization')).toBe('Bearer new-jwt');
    retriedRequest.flush([]);
  });

  it('logs out and rethrows when refresh fails', () => {
    authService.getToken.mockReturnValue('expired-jwt');
    authService.getRefreshToken.mockReturnValue('stored-refresh');
    authService.refresh.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' })));

    http.get('/api/projects').subscribe({
      error: () => undefined,
    });

    const firstRequest = httpMock.expectOne('/api/projects');
    firstRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authService.logout).toHaveBeenCalledTimes(1);
  });
});