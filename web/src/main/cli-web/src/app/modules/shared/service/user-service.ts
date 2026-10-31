import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../types/types';

@Service()
export class UserService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  saveUser(user: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.config.configValue.apiUrl}/users/saveUser`, user);
  }

  inactivateUsers(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/users/inactivateUsers`,
      ids,
    );
  }

  activateUsers(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/users/activateUsers`,
      ids,
    );
  }

  deleteUsers(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.config.configValue.apiUrl}/users/deleteUsers`, ids);
  }

  saveProfilePicture(formData: FormData): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/users/saveProfilePicture`,
      formData,
    );
  }

  loadProfilePicture(username: string): Observable<Blob> {
    return this.http.get(`${this.config.configValue.apiUrl}/users/${username}/profile-picture`, {
      responseType: 'blob',
    });
  }
}
