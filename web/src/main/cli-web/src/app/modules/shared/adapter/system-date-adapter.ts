import { effect, Injectable } from '@angular/core';
import { NativeDateAdapter } from '@angular/material/core';
import { SystemConfigService } from '../service/system-config-service';

@Injectable()
export class SystemDateAdapter extends NativeDateAdapter {
  constructor(private readonly systemConfig: SystemConfigService) {
    super();

    effect(() => {
      // Register the signal as a dependency.
      // When dateFormat changes, Angular reruns this effect.
      this.systemConfig.dateFormat();

      // Notify Angular Material that the adapter configuration changed.
      this.setLocale(this.locale);
    });
  }

  override format(date: Date, displayFormat: object): string {
    if (!date || !this.isValid(date)) {
      return '';
    }

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    if (this.systemConfig.dateFormat() === 'MM/dd/yyyy') {
      return `${month}/${day}/${year}`;
    }

    return `${day}/${month}/${year}`;
  }

  override parse(value: any, parseFormat: any): Date | null {
    if (!value) {
      return null;
    }

    if (value instanceof Date) {
      return value;
    }

    if (typeof value !== 'string') {
      return null;
    }

    const parts = value.split('/');

    if (parts.length !== 3) {
      return null;
    }

    let day: number;
    let month: number;
    let year: number;

    if (this.systemConfig.dateFormat() === 'MM/dd/yyyy') {
      month = Number(parts[0]);
      day = Number(parts[1]);
    } else {
      day = Number(parts[0]);
      month = Number(parts[1]);
    }

    year = Number(parts[2]);

    const date = new Date(year, month - 1, day);

    if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
      return null;
    }

    return date;
  }
}
