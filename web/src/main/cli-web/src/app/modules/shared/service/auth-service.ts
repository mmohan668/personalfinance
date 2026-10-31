import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';

import { ApiResponse, UserProfile } from '../types/types';
import { AppConfigService } from './app-config-service';
import { Router } from '@angular/router';
import { NotificationService } from './notification-service';
import { CommonService } from './common-service';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userProfile: UserProfile;
}

@Service()
export class AuthService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);
  private router = inject(Router);
  private _ns = inject(NotificationService);
  private _cs = inject(CommonService);

  isLoggedIn(): boolean {
    return !!localStorage.getItem('authToken');
  }

  storeToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  storeUserInfo(userProfile: UserProfile): void {
    localStorage.setItem('userProfile', JSON.stringify(userProfile));
    this._cs.setUserProfile(userProfile);
  }

  logout(): void {
    firstValueFrom(this.http.post<void>(`${this.config.configValue.apiUrl}/api/auth/logout`, {}))
      .then(() => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userProfile');

        this._ns.success('You have been logged out successfully.');
        this.router.navigate(['/login']);
      })
      .catch((error) => {
        console.error('Logout failed:', error);
        this._ns.error('Logout failed. Please try again.');
      });
  }

  authenticate(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.config.configValue.apiUrl}/api/auth/login`,
      request,
    );
  }

  signUp(user: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.config.configValue.apiUrl}/api/auth/signUp`, user);
  }
}
