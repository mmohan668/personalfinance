import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config-service';
import { ApiResponse, SelectItem } from '../types/types';

@Service()
export class CategoryManagementService {
  private http = inject(HttpClient);
  private config = inject(AppConfigService);

  fetchCategoryTypes(): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/categoryManagement/fetchCategoryTypes`,
    );
  }

  fetchCategories(referenceValueId: any): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/categoryManagement/fetchCategories`,
      {
        params: {
          adminUserId: 1,
          referenceValueId: referenceValueId,
        },
      },
    );
  }

  saveCategory(category: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/saveCategory`,
      category,
    );
  }

  saveSubcategory(subcategory: any): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/saveSubcategory`,
      subcategory,
    );
  }

  deleteCategories(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/deleteCategories`,
      ids,
    );
  }

  deleteSubcategories(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/deleteSubcategories`,
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

  activateSubcategories(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/activateSubcategories`,
      ids,
    );
  }

  inactivateSubcategories(ids: any[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/categoryManagement/inactivateSubcategories`,
      ids,
    );
  }
}
