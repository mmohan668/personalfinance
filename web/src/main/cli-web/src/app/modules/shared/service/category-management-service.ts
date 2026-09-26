import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config-service';
import { ApiResponse, SelectItem } from '../types/types';

@Service()
export class CategoryManagementService {
  private http = inject(HttpClient);
  private config = inject(AppConfigService);

  fetchTransactionTypes(): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/categoryManagement/fetchTransactionTypes`,
    );
  }

  fetchCategories(referenceValueId: any): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/categoryManagement/fetchCategories`,
      {
        params: {
          referenceValueId: referenceValueId,
        },
      },
    );
  }

  fetchCategoriesByReferenceCode(referenceCode: string): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/categoryManagement/fetchCategoriesByReferenceCode`,
      {
        params: {
          referenceCode: referenceCode,
        },
      },
    );
  }

  fetchSubcategoriesByCategory(categoryId: string): Observable<SelectItem[]> {
    return this.http.get<SelectItem[]>(
      `${this.config.configValue.apiUrl}/categoryManagement/fetchSubcategoriesByCategory`,
      {
        params: {
          categoryId: categoryId,
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
