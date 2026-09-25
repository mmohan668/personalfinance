import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonService } from '../../shared/service/common-service';
import { ApiResponse, SelectItem } from '../../shared/types/types';
import { firstValueFrom } from 'rxjs';
import { SettingsService } from '../../shared/service/settings-service';
import { NotificationService } from '../../shared/service/notification-service';
import { DATA_FIELDS, MODES } from '../../shared/enums';

@Component({
  selector: 'app-ad-edit-reference-value-dialog',
  standalone: true,
  imports: [CommonImportsModule],
  styleUrl: './ad-edit-reference-value-dialog.scss',
  templateUrl: './ad-edit-reference-value-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdEditReferenceValueDialog {
  private readonly dialogRef = inject(MatDialogRef<AdEditReferenceValueDialog>);
  readonly data = inject<any>(MAT_DIALOG_DATA);
  protected form!: FormGroup;
  public _cs = inject(CommonService);
  private _ss = inject(SettingsService);
  public categoryTypes = signal<SelectItem[]>([]);
  private _ns = inject(NotificationService);
  protected MODES = MODES;

  constructor() {
    this.fetchCategoryTypes();
    this.createForm();
    console.log(this.data);
  }

  fetchCategoryTypes() {
    firstValueFrom(this._ss.fetchRefObjNames()).then((res: SelectItem[]) => {
      this.categoryTypes.set(res);
    });
  }

  getEditVlue(fieldName: string): any {
    return this.data.mode === MODES.EDIT ? this.data.selectedRow[fieldName] : '';
  }

  createForm(): void {
    this.form = new FormGroup({
      referenceObjectName: new FormControl<number | string>(
        this.getEditVlue(DATA_FIELDS.REF_OBJ_NAME_ID),
        {
          nonNullable: true,
          validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
        },
      ),
      referenceCode: new FormControl<number | string>(
        this.getEditVlue(DATA_FIELDS.REFERENCE_CODE),
        {
          nonNullable: true,
          validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
        },
      ),
      referenceCodeDescription: new FormControl<number | string>(
        this.getEditVlue(DATA_FIELDS.REFERENCE_CODE_DESCRIPTION),
        {
          nonNullable: true,
          validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
        },
      ),
    });
  }

  save(): void {
    const referenceValue: any = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : null,
      refObjNameId: this.form.value.referenceObjectName,
      referenceCode: this.form.value.referenceCode.trim(),
      referenceCodeDescription: this.form.value.referenceCodeDescription.trim(),
    };
    firstValueFrom(this._ss.saveReferenceValue(referenceValue)).then((response: ApiResponse) => {
      console.log(response.success + ' : ' + response.message);
      if (response.success) {
        this._ns.success(response.message);
        this.dialogRef.close();
      } else {
        this._ns.error(response.message);
      }
    });
  }

  clear(): void {
    this._ns.close();
    if (this.data.mode === MODES.EDIT) {
      this.form.patchValue({
        referenceObjectName: this.data.selectedRow.refObjNameId,
        referenceCode: this.data.selectedRow.referenceCode,
        referenceCodeDescription: this.data.selectedRow.referenceCodeDescription,
      });
    } else {
      this.form.reset();
    }
  }

  close(): void {
    this._ns.close();
    this.dialogRef.close({
      action: 'close',
    });
  }
}
