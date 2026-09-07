import { Service } from '@angular/core';

@Service()
export class CommonService {
  activeInactiveBooleanOptions = [
    { label: 'All', value: null },
    { label: 'Active', value: true },
    { label: 'Inactive', value: false },
  ];

  isNotNull(value: any) {
    return value !== undefined && value !== null && value !== '';
  }
}
