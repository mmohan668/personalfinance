import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { ApiResponse, SelectItem } from '../types/types';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Service()
export class SettingsService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  saveReferenceValue(referenceValueDto: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/settings/saveReferenceValue`,
      referenceValueDto,
    );
  }

  deleteReferenceValue(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/settings/deleteReferenceValue`,
      ids,
    );
  }

  fetchCategoryTypes(): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/settings/fetchCategoryTypes`,
    );
  }
}
