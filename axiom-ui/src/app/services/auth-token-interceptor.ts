import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../core/services/auth.service';

function isAxiomApiRequest(url: string): boolean {
  if (url.startsWith('/api')) {
    return true;
  }
  try {
    if (/^https?:\/\//i.test(url)) {
      return new URL(url).pathname.startsWith('/api');
    }
  } catch {
    /* ignore invalid URL */
  }
  return false;
}

function matchesAuthPath(url: string, path: string): boolean {
  if (url === path) {
    return true;
  }

  try {
    if (/^https?:\/\//i.test(url)) {
      return new URL(url).pathname === path;
    }
  } catch {
    /* ignore invalid URL */
  }

  return false;
}

function isAuthEndpoint(url: string): boolean {
  return [
    '/api/auth/login',
    '/api/auth/register',
    '/api/auth/refresh-token',
  ].some((path) => matchesAuthPath(url, path));
}

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);

  if (isAuthEndpoint(request.url) || !isAxiomApiRequest(request.url)) {
    return next(request);
  }

  const token = authService.getToken();
  const authenticatedRequest = token
    ? request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    })
    : request;

  return next(
    authenticatedRequest,
  ).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      if (!authService.getRefreshToken()) {
        return throwError(() => error);
      }

      return authService.refresh().pipe(
        switchMap((response) =>
          next(
            request.clone({
              setHeaders: {
                Authorization: `Bearer ${response.token}`,
              },
            }),
          ),
        ),
        catchError((refreshError) => {
          authService.logout();
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
