import { Service } from '@angular/core';

@Service()
export class AuthService {
  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  login(token: string): void {
    localStorage.setItem('token', token);
  }

  logout(): void {
    localStorage.removeItem('token');
  }
}
