import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardSummary } from '../types/types';

@Service()
export class DashboardService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  fetchDashboardSummary(year: number, month: number): Observable<DashboardSummary> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.get<DashboardSummary>(
      `${this.config.configValue.apiUrl}/dashboard/fetchDashboardSummary`,
      { params },
    );
  }
}
