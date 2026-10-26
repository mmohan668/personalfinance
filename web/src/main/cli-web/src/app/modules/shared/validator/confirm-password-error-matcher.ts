import { AbstractControl, FormGroupDirective, NgForm } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';

export class ConfirmPasswordErrorMatcher implements ErrorStateMatcher {
  isErrorState(control: AbstractControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const interacted = !!(control && (control.dirty || control.touched));

    return !!(
      control &&
      interacted &&
      (control.invalid || control.parent?.hasError('passwordMismatch'))
    );
  }
}
