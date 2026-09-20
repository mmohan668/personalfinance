import { Service, signal } from '@angular/core';

export type CurrencyCode = 'INR' | 'USD';

@Service()
export class SystemConfigService {
  readonly currency = signal<CurrencyCode>('INR');

  readonly dateFormat = signal<string>('dd/MM/yyyy');

  setCurrency(currency: CurrencyCode): void {
    this.currency.set(currency);

    this.dateFormat.set(currency === 'INR' ? 'dd/MM/yyyy' : 'MM/dd/yyyy');
  }
}
