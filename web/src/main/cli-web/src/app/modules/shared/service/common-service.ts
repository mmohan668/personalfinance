import { Service } from '@angular/core';
import { ToolbarConfig } from '../types/types';
import { AbstractControl } from '@angular/forms';

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

  /**
   * Returns true when the control is invalid and has been touched.
   */
  isInvalid(control: AbstractControl | null): boolean {
    return !!(control && control.invalid && (control.touched || control.dirty));
  }

  /**
   * Returns a generic validation message for a control.
   */
  getValidationMessage(control: AbstractControl | null, fieldName: string): string {
    if (!control || !control.errors) {
      return '';
    }

    if (control.hasError('required')) {
      return `${fieldName} is required`;
    }

    if (control.hasError('maxlength')) {
      const maxLength = control.getError('maxlength')?.requiredLength;

      return `${fieldName} cannot exceed ${maxLength} characters`;
    }

    if (control.hasError('minlength')) {
      const minLength = control.getError('minlength')?.requiredLength;

      return `${fieldName} must be at least ${minLength} characters`;
    }

    if (control.hasError('email')) {
      return `Please enter a valid email address`;
    }

    if (control.hasError('min')) {
      const min = control.getError('min')?.min;

      return `${fieldName} must be at least ${min}`;
    }

    if (control.hasError('max')) {
      const max = control.getError('max')?.max;

      return `${fieldName} must be at most ${max}`;
    }

    if (control.hasError('pattern')) {
      return `${fieldName} has an invalid format`;
    }

    return `${fieldName} is invalid`;
  }
}
