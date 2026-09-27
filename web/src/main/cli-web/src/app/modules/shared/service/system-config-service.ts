import { inject, Service, signal } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import {
  DEFAULT_CURRENCY_CODE,
  DEFAULT_CURRENCY_LOCALE,
  DEFAULT_DATE_FORMAT,
  DEFAULT_DATE_TIME_FORMAT,
  TIME_FORMAT,
} from '../constants';
import { SystemConfigValue } from '../types/types';
import { CommonService } from './common-service';

export type CurrencyCode = 'INR' | 'USD';

@Service()
export class SystemConfigService {
  readonly currency = signal<string>(DEFAULT_CURRENCY_CODE);
  readonly locale = signal<string>(DEFAULT_CURRENCY_LOCALE);
  readonly dateFormat = signal<string>(DEFAULT_DATE_FORMAT);
  readonly dateTimeFormat = signal<string>(DEFAULT_DATE_TIME_FORMAT);

  private config = inject(AppConfigService);
  private http = inject(HttpClient);
  private _cs = inject(CommonService);

  constructor() {
    this.fetchSystemConfig();
  }

  fetchSystemConfig(): void {
    firstValueFrom(
      this.http.get<any>(`${this.config.configValue.apiUrl}/common/fetchSystemConfig`),
    ).then((systemConfigValue: SystemConfigValue) => {
      this.currency.set(systemConfigValue.currencyCode);
      this.locale.set(systemConfigValue.locale);
      if (this._cs.isNotNull(systemConfigValue.dateFormat)) {
        const dateFormat = systemConfigValue.dateFormat
          .replace(/YYYY/g, 'yyyy')
          .replace(/DD/g, 'dd');
        this.dateFormat.set(dateFormat);
        this.dateTimeFormat.set(dateFormat + TIME_FORMAT);
      }
    });
  }
}
