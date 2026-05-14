import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../core/services/auth.service';
import { authTokenInterceptor } from './auth-token-interceptor';

describe('authTokenInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: { getToken: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = {
      getToken: vi.fn(),
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
});
