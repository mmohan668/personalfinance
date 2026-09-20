import { Service } from '@angular/core';

@Service()
export class DateFormatService {
  private format = 'DD/MM/YYYY'; // default

  setFormat(fmt: string) {
    this.format = fmt;
  }

  getFormat(): string {
    return this.format;
  }
}
