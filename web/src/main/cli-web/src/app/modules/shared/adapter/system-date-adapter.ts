import { effect, Injectable } from '@angular/core';
import { NativeDateAdapter } from '@angular/material/core';
import { SystemConfigService } from '../service/system-config-service';

@Injectable()
export class SystemDateAdapter extends NativeDateAdapter {
  constructor(private readonly systemConfig: SystemConfigService) {
    super();

    effect(() => {
      // Register the signal as a dependency.
      this.systemConfig.dateFormat();

      // Notify Angular Material that the adapter configuration changed.
      this.setLocale(this.locale);
    });
  }

  override format(date: Date, displayFormat: object): string {
    if (!date || !this.isValid(date)) {
      return '';
    }

    const format = this.systemConfig.dateFormat();

    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    const monthsShort = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];

    const monthsLong = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];

    const values: Record<string, string> = {
      dd: String(day).padStart(2, '0'),
      d: String(day),

      MM: String(month).padStart(2, '0'),
      M: String(month),

      yy: String(year).slice(-2),
      yyyy: String(year),

      MMM: monthsShort[month - 1],
      MMMM: monthsLong[month - 1],
    };

    return this.replaceDateTokens(format, values);
  }

  override parse(value: any, parseFormat: any): Date | null {
    if (!value) {
      return null;
    }

    if (value instanceof Date) {
      return this.isValid(value) ? value : null;
    }

    if (typeof value !== 'string') {
      return null;
    }

    const format = this.systemConfig.dateFormat();

    return this.parseDate(value.trim(), format);
  }

  private replaceDateTokens(format: string, values: Record<string, string>): string {
    // Longest tokens first so MMMM is not partially replaced by MMM.
    return format.replace(/yyyy|MMMM|MMM|MM|dd|yy|M|d/g, (token) => values[token]);
  }

  private parseDate(value: string, format: string): Date | null {
    const tokens = this.getDateTokens(format);

    if (tokens.length !== 3) {
      return null;
    }

    // Convert the configured date format into a regex.
    let regexPattern = format;

    // Escape regex characters.
    regexPattern = regexPattern.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

    // Replace escaped date tokens with capture groups.
    regexPattern = regexPattern
      .replace(/yyyy/g, '(\\d{4})')
      .replace(/MMMM/g, '([A-Za-z]+)')
      .replace(/MMM/g, '([A-Za-z]+)')
      .replace(/MM/g, '(\\d{1,2})')
      .replace(/dd/g, '(\\d{1,2})')
      .replace(/yy/g, '(\\d{2})')
      .replace(/M/g, '(\\d{1,2})')
      .replace(/d/g, '(\\d{1,2})');

    const match = new RegExp(`^${regexPattern}$`).exec(value);

    if (!match) {
      return null;
    }

    const parsed: {
      day?: number;
      month?: number;
      year?: number;
    } = {};

    let matchIndex = 1;

    for (const token of tokens) {
      const part = match[matchIndex++];

      switch (token) {
        case 'dd':
        case 'd':
          parsed.day = Number(part);
          break;

        case 'MM':
        case 'M':
          parsed.month = Number(part);
          break;

        case 'MMM':
          parsed.month = this.parseMonthName(part, false);
          break;

        case 'MMMM':
          parsed.month = this.parseMonthName(part, true);
          break;

        case 'yyyy':
          parsed.year = Number(part);
          break;

        case 'yy':
          parsed.year = this.parseTwoDigitYear(part);
          break;
      }
    }

    if (parsed.day == null || parsed.month == null || parsed.year == null) {
      return null;
    }

    return this.createValidDate(parsed.year, parsed.month, parsed.day);
  }

  private getDateTokens(format: string): string[] {
    return format.match(/yyyy|MMMM|MMM|MM|dd|yy|M|d/g) ?? [];
  }

  private parseMonthName(value: string, longName: boolean): number {
    const months = longName
      ? [
          'january',
          'february',
          'march',
          'april',
          'may',
          'june',
          'july',
          'august',
          'september',
          'october',
          'november',
          'december',
        ]
      : ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];

    const index = months.indexOf(value.toLowerCase());

    return index === -1 ? NaN : index + 1;
  }

  private parseTwoDigitYear(value: string): number {
    const year = Number(value);

    // Same general convention used by many date libraries:
    // 00-68 => 2000-2068
    // 69-99 => 1969-1999
    return year <= 68 ? 2000 + year : 1900 + year;
  }

  private createValidDate(year: number, month: number, day: number): Date | null {
    if (
      !Number.isInteger(year) ||
      !Number.isInteger(month) ||
      !Number.isInteger(day) ||
      month < 1 ||
      month > 12 ||
      day < 1 ||
      day > 31
    ) {
      return null;
    }

    const date = new Date(year, month - 1, day);

    // Prevent JavaScript from normalizing invalid dates.
    // Example: 31/02/2026 -> March 3rd would otherwise be accepted.
    if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
      return null;
    }

    return date;
  }
}
