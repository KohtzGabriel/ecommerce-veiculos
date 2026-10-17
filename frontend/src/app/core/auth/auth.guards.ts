import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthRole } from './auth.models';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const role = route.data['role'] as AuthRole;

  if (!auth.isAuthenticated()) {
    return router.parseUrl(role === 'ADMINISTRADOR' ? '/admin/login' : '/cliente/login');
  }
  if (auth.hasRole(role)) return true;
  return router.parseUrl(auth.hasRole('ADMINISTRADOR') ? '/admin/home' : '/loja');
};
