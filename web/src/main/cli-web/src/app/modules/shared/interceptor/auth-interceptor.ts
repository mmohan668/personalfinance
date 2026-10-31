import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('authToken');

  if (!token) {
    return next(req);
  }

  const authenticatedRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authenticatedRequest).pipe(
    tap((event) => {
      if ('headers' in event) {
        const renewedToken = event.headers.get('X-New-Access-Token');
        if (renewedToken) {
          localStorage.setItem('authToken', renewedToken);
        }
      }
    }),
  );
};
