import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface AppConfig {
  apiUrl: string;
  environment: string;
  pageSizeOptions: number[];
  defaultPageSize: number;
  maxExportRows: number;
  maxSuggestionRows: number;
  debounceTime: number;
}

@Service()
export class AppConfigService {
  private http = inject(HttpClient);
  private appConfig!: AppConfig;

  setConfig(appConfig: AppConfig) {
    this.appConfig = appConfig;
  }

  loadFromWebService(): Observable<AppConfig> {
    const basePath = new URL(document.baseURI).pathname.replace(/\/+$/, '');
    return this.http.get<AppConfig>(`${basePath}/api/config`).pipe(
      tap((config) => this.setConfig(config)),
    );
  }

  get configValue(): AppConfig {
    if (!this.appConfig) {
      return {
        apiUrl: 'https://bouncing-broadside-mustang.ngrok-free.dev/pf-warehouse/api',
        environment: 'Dev',
        pageSizeOptions: [25, 50, 100],
        defaultPageSize: 25,
        maxExportRows: 1,
        maxSuggestionRows: 50,
        debounceTime: 500,
      };
    }
    return this.appConfig;
  }
}
