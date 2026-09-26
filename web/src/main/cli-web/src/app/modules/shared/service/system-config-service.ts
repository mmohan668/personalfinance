import { Service, signal } from '@angular/core';

export type CurrencyCode = 'INR' | 'USD';

@Service()
export class SystemConfigService {
  readonly currency = signal<CurrencyCode>('USD');
  readonly locale = signal<string>('en-IN');
  readonly dateFormat = signal<string>('dd/MM/yyyy');
  readonly dateTimeFormat = signal<string>('dd/MM/yyyy HH:mm:ss');

  setCurrency(currency: CurrencyCode): void {
    this.currency.set(currency);
    this.dateFormat.set(currency === 'INR' ? 'dd/MM/yyyy' : 'MM/dd/yyyy');
    this.dateTimeFormat.set(currency === 'INR' ? 'dd/MM/yyyy HH:mm:ss' : 'MM/dd/yyyy HH:mm:ss');
  }
}
