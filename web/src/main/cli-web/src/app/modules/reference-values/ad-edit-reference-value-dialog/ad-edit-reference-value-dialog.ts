import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonService } from '../../shared/service/common-service';
import { ApiResponse, SelectItem } from '../../shared/types/types';
import { firstValueFrom } from 'rxjs';
import { SettingsService } from '../../shared/service/settings-service';
import { NotificationService } from '../../shared/service/notification-service';

@Component({
  selector: 'app-ad-edit-reference-value-dialog',
  standalone: true,
  imports: [CommonImportsModule],
  styleUrl: './ad-edit-reference-value-dialog.scss',
  templateUrl: './ad-edit-reference-value-dialog.html',
})
export class AdEditReferenceValueDialog {
  private readonly dialogRef = inject(MatDialogRef<AdEditReferenceValueDialog>);
  readonly data = inject<any>(MAT_DIALOG_DATA);
  form: FormGroup = new FormGroup({});
  public _cs = inject(CommonService);
  private _ss = inject(SettingsService);
  public categoryTypes!: SelectItem[];
  private _ns = inject(NotificationService);

  constructor() {
    this.createForm();
    this.categoryTypes = this.data.categoryTypes;
  }

  createForm() {
    this.form.addControl(
      'referenceObjectName',
      new FormControl(this.data.mode === 'edit' ? this.data.referenceObjectName : '', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
      ]),
    );
    this.form.addControl(
      'referenceCode',
      new FormControl(this.data.mode === 'Add' ? this.data.referenceCode : '', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
      ]),
    );
    this.form.addControl(
      'referenceCodeDescription',
      new FormControl(this.data.mode === 'Add' ? this.data.referencecodeDescription : '', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(200),
      ]),
    );
  }

  save(): void {
    const referenceValue: any = {
      id: this.data.mode === 'Edit' ? 1 : null,
      refObjNameId: this.form.value.referenceObjectName,
      referenceCode: this.form.value.referenceCode,
      referenceCodeDescription: this.form.value.referenceCodeDescription,
    };
    firstValueFrom(this._ss.saveReferenceValue(referenceValue))
      .then((response: ApiResponse) => {
        console.log(response.success + ' : ' + response.message);
        if (response.success) {
          this._ns.success(response.message);
          this.dialogRef.close();
        } else {
          this._ns.error(response.message);
        }
      })
      .catch((error) => {
        console.log('Error while saveReferenceValue', error);
      });
  }

  clear(): void {
    this.form.reset();
  }

  close(): void {
    this.dialogRef.close({
      action: 'close',
    });
  }
}
