import { Component, inject, signal } from '@angular/core';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonService } from '../../shared/service/common-service';
import { ApiResponse, SelectItem } from '../../shared/types/types';
import { SettingsService } from '../../shared/service/settings-service';
import { firstValueFrom } from 'rxjs';
import { NotificationService } from '../../shared/service/notification-service';
import { REF_OBJ_NAMES, SYSTEM_CONFIG_NAME } from '../../shared/enums';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-update-system-config-dialog',
  styleUrl: './update-system-config-dialog.scss',
  templateUrl: './update-system-config-dialog.html',
})
export class UpdateSystemConfigDialog {
  protected readonly dialogRef = inject(MatDialogRef<this>);
  protected readonly data = inject<any>(MAT_DIALOG_DATA);
  protected readonly _cs = inject(CommonService);
  private readonly _ss = inject(SettingsService);
  private readonly _ns = inject(NotificationService);

  protected readonly referenceCodes = signal<SelectItem[]>([]);

  protected form!: FormGroup;

  refObjNameByConfig = {
    [SYSTEM_CONFIG_NAME.CURRENCY]: REF_OBJ_NAMES.CURRENCY_CODE,
    [SYSTEM_CONFIG_NAME.DATE_FORMAT]: REF_OBJ_NAMES.DATE_FORMAT,
  };

  constructor() {
    this.fetchReferenceCodes();
    this.form = new FormGroup({
      configValue: new FormControl<number | string>(this.data.rowData.referenceId, {
        nonNullable: true,
        validators: [Validators.required],
      }),
    });
  }

  fetchReferenceCodes(): void {
    const refObjName = this.refObjNameByConfig[this.data.configName as SYSTEM_CONFIG_NAME];
    if (!refObjName) return;

    firstValueFrom(this._ss.fetchReferenceValuesByRefObjName(refObjName)).then(
      (resp: SelectItem[]) => {
        this.referenceCodes.set(resp);
      },
    );
  }

  save(): void {
    const systemConfigDto = this.data.rowData;
    systemConfigDto['referenceId'] = this.form.value.configValue;
    firstValueFrom(this._ss.saveSystemConfig(systemConfigDto)).then((resp: ApiResponse) => {
      if (resp.success) {
        this.dialogRef.close(resp.message);
      } else {
        this._ns.error(resp.message);
      }
    });
  }
}
