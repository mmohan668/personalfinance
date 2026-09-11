import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { ApiResponse, ReferenceObject } from '../types/types';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Service()
export class SettingsService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  saveReferenceObject(referenceObject: ReferenceObject): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/settings/saveReferenceObject`,
      referenceObject,
    );
  }
}
