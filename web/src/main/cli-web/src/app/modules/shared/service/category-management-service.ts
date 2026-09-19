import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { SelectItem } from 'primeng/api';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config-service';
import { ApiResponse } from '../types/types';

@Service()
export class CategoryManagementService {
  private http = inject(HttpClient);
  private config = inject(AppConfigService);

  fetchCategoryTypes(): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/categoryManagement/fetchCategoryTypes`,
    );
  }

  saveCategory(category: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/saveCategory`,
      category,
    );
  }

  deleteCategories(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/deleteCategories`,
      ids,
    );
  }

  activateCategories(ids: any[], activateSubcategories: boolean = false): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/activateCategories`,
      ids,
      {
        params: {
          activateSubcategories: activateSubcategories,
        },
      },
    );
  }

  inactivateCategories(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/inactivateCategories`,
      ids,
    );
  }
}
