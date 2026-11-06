import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { tap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('authToken');
  const requestPath = req.url.split(/[?#]/, 1)[0].replace(/\/+$/, '').toLowerCase();
  const isPublicAuthRequest = ['/auth/login', '/auth/signup', '/auth/forgot-password'].some(
    (endpoint) => requestPath.endsWith(endpoint),
  );

  const authenticatedRequest =
    token && !isPublicAuthRequest
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
          sessionStorage.setItem('authToken', renewedToken);
        }
      }
    }),
  );
};
