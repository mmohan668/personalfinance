import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { AppConfigService } from './app-config-service';
import { ApiResponse } from '../types/types';

export interface RecurringTransactionRule {
  id?: number;
  createdById?: number;
  createdBy?: string;
  createdAt?: string;
  updatedBy?: string | null;
  updatedAt?: string | null;
  ruleName: string;
  subcategoryId: number;
  categoryId: number;
  transactionType?: string;
  categoryName?: string;
  subcategoryName?: string;
  locationId: number | null;
  amount: number;
  remarks: string | null;
  frequency: string;
  intervalValue: number;
  dayOfWeek: number | null;
  dayOfMonth: number | null;
  monthOfYear: number | null;
  monthlyPattern: string | null;
  startDate: string;
  endDate: string | null;
  executionTime: string;
  active?: boolean;
}

@Service()
export class RecurringTransactionsService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(AppConfigService);
  private readonly url = `${this.config.configValue.apiUrl}/recurringTransactions`;

  fetchRules(): Observable<RecurringTransactionRule[]> {
    return this.http.get<RecurringTransactionRule[]>(this.url);
  }

  saveRule(rule: RecurringTransactionRule): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.url}/save`, rule);
  }

  deleteRules(ruleIds: number[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.url}/delete`, ruleIds);
  }

  setRulesActive(ruleIds: number[], active: boolean): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.url}/setActive`, { ruleIds, active });
  }
}
