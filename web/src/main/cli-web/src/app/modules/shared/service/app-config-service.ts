import { Service } from '@angular/core';

export interface AppConfig {
  apiUrl: string;
  environment: string;
  pageSizeOptions: number[];
  defaultPageSize: number;
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
        apiUrl: 'http://localhost:1003',
        environment: 'Dev',
        pageSizeOptions: [25, 50, 100],
        defaultPageSize: 25,
      };
    }
    return this.appConfig;
  }
}
