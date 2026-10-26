import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function passwordMatchValidator(
  passwordField = 'password',
  confirmPasswordField = 'confirmPassword',
  skipValue?: string,
): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const passwordControl = group.get(passwordField);
    const confirmControl = group.get(confirmPasswordField);

    if (!passwordControl || !confirmControl) {
      return null;
    }

    const password = passwordControl.value;
    const confirmPassword = confirmControl.value;

    if (skipValue !== undefined && password === skipValue) {
      return null;
    }

    if (!password || !confirmPassword || password === confirmPassword) {
      return null;
    }

    return { passwordMismatch: true };
  };
}
