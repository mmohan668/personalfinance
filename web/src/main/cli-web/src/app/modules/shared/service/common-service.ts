import { inject, Service } from '@angular/core';
import { ToolbarConfig } from '../types/types';
import { AbstractControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from './notification-service';

@Service()
export class CommonService {
  activeInactiveBooleanOptions = [
    { label: 'All', value: null },
    { label: 'Active', value: true },
    { label: 'Inactive', value: false },
  ];

  private dialog = inject(MatDialog);
  private _ns = inject(NotificationService);

  constructor() {
    this.dialog.afterOpened.subscribe(() => this._ns.close());
    this.dialog.afterAllClosed.subscribe(() => this._ns.close());
  }

  isNotNull(value: any) {
    return value !== undefined && value !== null && value !== '';
  }

  toolbarConfig(value: boolean = false): ToolbarConfig {
    return {
      addRow: value,
      copyRow: value,
      deleteRow: value,
      editRow: value,
      activate: value,
      inactivate: value,
    };
  }

  toolbarConfigExcludeActivateInactivate(): ToolbarConfig {
    return {
      addRow: true,
      copyRow: true,
      deleteRow: true,
      editRow: true,
      activate: false,
      inactivate: false,
    };
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

    // Angular Material datepicker
    if (control.hasError('matDatepickerMax')) {
      return `${fieldName} must be on or before ${this.formatDateOnly(
        control.getError('matDatepickerMax')?.max,
      )}`;
    }

    if (control.hasError('matDatepickerMin')) {
      return `${fieldName} must be on or after ${this.formatDateOnly(
        control.getError('matDatepickerMin')?.min,
      )}`;
    }

    return `${fieldName} is invalid`;
  }

  onAmountInput(event: Event, controlName: string, form: FormGroup): void {
    const input = event.target as HTMLInputElement;

    let value = input.value;

    // Allow only numbers, '-' and '.'
    value = value.replace(/[^0-9.-]/g, '');

    // '-' is allowed only at the beginning
    if (value.includes('-')) {
      value = '-' + value.replace(/-/g, '');
    }

    // Allow only one '.'
    const firstDotIndex = value.indexOf('.');
    if (firstDotIndex !== -1) {
      value =
        value.substring(0, firstDotIndex + 1) +
        value.substring(firstDotIndex + 1).replace(/\./g, '');
    }

    // Allow maximum 4 digits after '.'
    const decimalIndex = value.indexOf('.');
    if (decimalIndex !== -1) {
      value =
        value.substring(0, decimalIndex + 1) + value.substring(decimalIndex + 1, decimalIndex + 5);
    }

    input.value = value;

    form.get(controlName)?.setValue(value, {
      emitEvent: false,
    });
  }

  formatDateOnly(date: Date): string | null {
    if (!date) {
      return null;
    }

    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  trimDesc(value: string) {
    return this.isNotNull(value) && value.includes(' ~ ')
      ? value.substring(0, value.indexOf(' ~ '))
      : value;
  }
}
