import { Service } from '@angular/core';

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
  private appConfig!: AppConfig;

  setConfig(appConfig: AppConfig) {
    this.appConfig = appConfig;
  }

  get configValue(): AppConfig {
    if (!this.appConfig) {
      return {
        apiUrl: 'http://192.168.0.101:1991/pf-warehouse/api',
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
