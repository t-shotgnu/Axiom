import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthApi } from './auth-api';

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

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthApi).token;

  if (!token || !isAxiomApiRequest(request.url)) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
