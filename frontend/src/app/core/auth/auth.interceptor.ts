import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from './auth-api.service';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const isLogin = request.url.endsWith('/auth/login');
  const token = !isLogin && request.url.startsWith(API_BASE_URL) ? auth.accessToken() : null;
  const outgoing = token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;

  return next(outgoing).pipe(catchError((error: unknown) => {
    if (error instanceof HttpErrorResponse && error.status === 401 && !isLogin && auth.isAuthenticated()) {
      auth.logout();
      const loginUrl = router.url.startsWith('/admin') ? '/admin/login' : '/cliente/login';
      void router.navigateByUrl(loginUrl);
    }
    return throwError(() => error);
  }));
};
