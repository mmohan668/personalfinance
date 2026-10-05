import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonService } from '../../shared/service/common-service';
import { SettingsService } from '../../shared/service/settings-service';
import { ApiResponse } from '../../shared/types/types';
import { NotificationService } from '../../shared/service/notification-service';
import { DATA_FIELDS, MODES } from '../../shared/enums';
import { firstValueFrom } from 'rxjs';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-add-edit-reference-object-dialog',
  styleUrl: './add-edit-reference-object-dialog.scss',
  templateUrl: './add-edit-reference-object-dialog.html',
})
export class AddEditReferenceObjectDialog {
  protected readonly dialogRef = inject(MatDialogRef<AddEditReferenceObjectDialog>);
  readonly data = inject<any>(MAT_DIALOG_DATA);
  protected form!: FormGroup;
  public _cs = inject(CommonService);
  private _ss = inject(SettingsService);
  private _ns = inject(NotificationService);
  protected MODES = MODES;

  constructor() {
    this.createForm();
  }

  getEditVlue(fieldName: string): any {
    return this.data.mode === MODES.EDIT ? this.data.selectedRow[fieldName] : '';
  }

  createForm(): void {
    this.form = new FormGroup({
      refObjName: new FormControl<number | string>(this.getEditVlue(DATA_FIELDS.REF_OBJ_NAME), {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
      }),
    });
  }

  save(): void {
    const referenceObject: any = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : null,
      refObjName: this.form.value.refObjName,
    };
    firstValueFrom(this._ss.saveReferenceObject(referenceObject)).then((response: ApiResponse) => {
      if (response.success) {
        this.dialogRef.close(response.message);
      } else {
        this._ns.error(response.message);
      }
    });
  }
}
