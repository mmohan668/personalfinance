import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { tap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('authToken');

  const authenticatedRequest = token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    : req;

  return next(authenticatedRequest).pipe(
    tap((event) => {
      if (event instanceof HttpResponse) {
        const renewedToken = event.headers.get('X-New-Access-Token');
        if (renewedToken) {
          localStorage.setItem('authToken', renewedToken);
        }
      }
    }),
  );
};
