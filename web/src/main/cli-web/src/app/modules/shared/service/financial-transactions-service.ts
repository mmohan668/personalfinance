import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { HttpClient } from '@angular/common/http';
import { ApiResponse, FinancialTransactionDto } from '../types/types';
import { Observable } from 'rxjs';

@Service()
export class FinancialTransactionsService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  saveFinancialTransaction(
    financialTransactionDto: FinancialTransactionDto,
  ): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/financialTransaction/saveFinancialTransaction`,
      financialTransactionDto,
    );
  }
}
