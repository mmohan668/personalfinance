import { HttpInterceptorFn } from '@angular/common/http';

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

  return next(authenticatedRequest);
};
