import { Service } from '@angular/core';
import { ToolbarConfig } from '../types/types';

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

  get toolbarConfig(): ToolbarConfig {
    return { addRow: false, copyRow: false, deleteRow: false, editRow: false };
  }
}
