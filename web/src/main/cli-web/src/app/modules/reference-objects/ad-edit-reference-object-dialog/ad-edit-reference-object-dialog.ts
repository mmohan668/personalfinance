import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonService } from '../../shared/service/common-service';
import { ApiResponse, ReferenceObject } from '../../shared/types/types';
import { firstValueFrom } from 'rxjs';
import { SettingsService } from '../../shared/service/settings-service';

@Component({
  selector: 'app-ad-edit-reference-object-dialog',
  standalone: true,
  imports: [CommonImportsModule],
  styleUrl: './ad-edit-reference-object-dialog.scss',
  templateUrl: './ad-edit-reference-object-dialog.html',
})
export class AdEditReferenceObjectDialog {
  private readonly dialogRef = inject(MatDialogRef<AdEditReferenceObjectDialog>);
  readonly data = inject<any>(MAT_DIALOG_DATA);
  form: FormGroup = new FormGroup({});
  public _cs = inject(CommonService);
  private _ss = inject(SettingsService);

  constructor() {
    this.form.addControl(
      'referenceObjectName',
      new FormControl(this.data.mode === 'edit' ? this.data.referenceObjectName : '', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
      ]),
    );
  }

  save(): void {
    const referenceObject: ReferenceObject = {
      id: this.data.mode === 'edit' ? 1 : null,
      refObjName: this.form.value.referenceObjectName,
    };
    firstValueFrom(this._ss.saveReferenceObject(referenceObject))
      .then((response: ApiResponse) => {
        console.log(response.success + ' : ' + response.message);
        this.dialogRef.close();
      })
      .catch((error) => {
        console.log('Error while saveReferenceObject', error);
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
