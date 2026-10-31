import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../service/notification-service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const ns = inject(NotificationService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message = 'An unexpected error occurred. Please contact system administrator.';

      if (error.error && typeof error.error === 'object' && error.error.message) {
        message = error.error.message;
      } else if (error.status === 0) {
        message = 'Unable to connect to the server.';
      } else if (error.status === 400) {
        message = 'Invalid request.';
      } else if (error.status === 401) {
        message = 'Your session has expired. Please sign in again.';

        if (!req.url.includes('/api/auth/login')) {
          localStorage.removeItem('authToken');
          localStorage.removeItem('userProfile');
          void router.navigate(['/login']);
        }
      } else if (error.status === 403) {
        message = 'Access denied.';
      } else if (error.status === 404) {
        message = 'Requested resource was not found.';
      } else if (error.status >= 500) {
        message = 'An unexpected server error occurred. Please contact system administrator.';
      }

      console.error(error);
      ns.error(message);

      return throwError(() => error);
    }),
  );
};
