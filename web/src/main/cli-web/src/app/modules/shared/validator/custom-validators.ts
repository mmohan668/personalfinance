import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const VALID_DATE_FORMATS = [
  'dd-MM-yyyy',
  'dd.MM.yyyy',
  'dd/MM/yyyy',
  'dd MM yyyy',
  'ddMMMyyyy',
  'dd MMM yyyy',
  'dd-MMM-yyyy',
  'dd.MMM.yyyy',
  'dd/MMM/yyyy',
  'dd MMMM yyyy',
  'dd-MMMM-yyyy',
  'dd.MMMM.yyyy',
  'dd/MMMM/yyyy',

  'MM-dd-yyyy',
  'MM.dd.yyyy',
  'MM/dd/yyyy',
  'MM dd yyyy',
  'MMMdyyyy',
  'MMM dd yyyy',
  'MMM-dd-yyyy',
  'MMM.dd.yyyy',
  'MMM/dd/yyyy',
  'MMMM dd yyyy',
  'MMMM-dd-yyyy',
  'MMMM.dd.yyyy',
  'MMMM/dd/yyyy',

  'yyyy-MM-dd',
  'yyyy.MM.dd',
  'yyyy/MM/dd',
  'yyyy MM dd',
  'yyyyMMdd',
  'yyyyMMMdd',
  'yyyy MMM dd',
  'yyyy-MMM-dd',
  'yyyy.MMM.dd',
  'yyyy/MMM/dd',
  'yyyyMMMMdd',
  'yyyy MMMM dd',
  'yyyy-MMMM-dd',
  'yyyy.MMMM.dd',
  'yyyy/MMMM/dd',

  'dd-MM-yy',
  'dd.MM.yy',
  'dd/MM/yy',
  'dd MM yy',

  'MM-dd-yy',
  'MM.dd.yy',
  'MM/dd/yy',
  'MM dd yy',

  'yy-MM-dd',
  'yy.MM.dd',
  'yy/MM/dd',
  'yy MM dd',
] as const;

export class CustomValidators {
  static dateFormat(validFormats: readonly string[] = VALID_DATE_FORMATS): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value?.trim();

      // Optional field.
      // Validators.required should be used separately if needed.
      if (!value) {
        return null;
      }

      if (!validFormats.includes(value)) {
        return {
          dateFormat: {
            actual: value,
            validFormats,
          },
        };
      }

      return null;
    };
  }
}
