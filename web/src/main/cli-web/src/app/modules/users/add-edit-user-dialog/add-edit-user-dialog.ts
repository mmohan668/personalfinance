import { Component, inject, OnInit } from '@angular/core';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonService } from '../../shared/service/common-service';
import { NotificationService } from '../../shared/service/notification-service';
import { DATA_FIELDS, MODES } from '../../shared/enums';
import { firstValueFrom } from 'rxjs';
import { ApiResponse } from '../../shared/types/types';
import { passwordMatchValidator } from '../../shared/validator/password-match.validator';
import { ConfirmPasswordErrorMatcher } from '../../shared/validator/confirm-password-error-matcher';
import { UserService } from '../../shared/service/user-service';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-add-edit-user-dialog',
  styleUrl: './add-edit-user-dialog.scss',
  templateUrl: './add-edit-user-dialog.html',
})
export class AddEditUserDialog {
  protected readonly dialogRef = inject(MatDialogRef<AddEditUserDialog>);
  readonly data = inject<any>(MAT_DIALOG_DATA);
  public _cs = inject(CommonService);
  private _ns = inject(NotificationService);
  private _us = inject(UserService);

  protected form!: FormGroup;
  protected MODES = MODES;

  public readonly confirmPasswordMatcher = new ConfirmPasswordErrorMatcher();

  constructor() {
    this.createForm();
  }

  getEditVlue(fieldName: string): any {
    return this.data.mode == MODES.EDIT ? this.data.selectedRow[fieldName] : null;
  }

  createForm(): void {
    if (this.data.mode === MODES.ADD) {
      this.form = new FormGroup(
        {
          username: new FormControl<number | string>('', {
            nonNullable: true,
            validators: [Validators.required, Validators.minLength(3), Validators.maxLength(30)],
          }),
          displayName: new FormControl<number | string>('', {
            nonNullable: true,
            validators: [Validators.required, Validators.minLength(3), Validators.maxLength(30)],
          }),
          email: new FormControl<number | string>('', {
            nonNullable: true,
            validators: [Validators.required, Validators.email],
          }),
          password: new FormControl<number | string>('', {
            nonNullable: true,
            validators: [
              Validators.required,
              Validators.pattern(/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,16}$/),
            ],
          }),
          confirmPassword: new FormControl<number | string>('', {
            nonNullable: true,
            validators: [Validators.required],
          }),
        },
        {
          validators: passwordMatchValidator('password', 'confirmPassword'),
        },
      );
    } else if (this.data.mode === MODES.EDIT) {
      this.form = new FormGroup({
        username: new FormControl<number | string>(this.data.selectedRow[DATA_FIELDS.USERNAME], {
          nonNullable: true,
        }),
        displayName: new FormControl<number | string>(
          this.data.selectedRow[DATA_FIELDS.DISPLAY_NAME],
          {
            nonNullable: true,
            validators: [Validators.required, Validators.minLength(3), Validators.maxLength(30)],
          },
        ),
        email: new FormControl<number | string>(this.data.selectedRow[DATA_FIELDS.EMAIL], {
          nonNullable: true,
          validators: [Validators.required, Validators.email],
        }),
        password: new FormControl<number | string>('********', {
          nonNullable: true,
        }),
      });
      this.form.get('username')?.disable();
      this.form.get('password')?.disable();
    }
  }

  restrictUsernameCharacters(event: KeyboardEvent): void {
    const allowedKeys = [
      'Backspace',
      'Delete',
      'Tab',
      'Enter',
      'ArrowLeft',
      'ArrowRight',
      'Home',
      'End',
    ];

    if (allowedKeys.includes(event.key) || event.ctrlKey || event.metaKey) {
      return;
    }

    if (!/^[a-zA-Z0-9@_-]$/.test(event.key)) {
      event.preventDefault();
    }
  }

  save(): void {
    const user: any = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : null,
      username: this.data.mode === MODES.ADD ? this.form.value.username.trim() : null,
      displayName: this.form.value.displayName.trim(),
      email: this.form.value.email.trim(),
      password: this.data.mode === MODES.ADD ? this.form.value.password.trim() : null,
    };
    firstValueFrom(this._us.saveUser(user)).then((response: ApiResponse) => {
      if (response.success) {
        this.dialogRef.close(response.message);
      } else {
        this._ns.error(response.message);
      }
    });
  }
}
