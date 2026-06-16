import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const employeeGuardGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const role = authService.userRole();
  if (authService.isAuthenticated() && (role === 'EMPLOYEE' || role === 'ADMIN')) {
    return true;
  }

  router.navigate(['/home']);
  return false;
};
