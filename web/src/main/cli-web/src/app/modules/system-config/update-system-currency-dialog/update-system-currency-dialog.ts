import { Component, inject, signal } from '@angular/core';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonService } from '../../shared/service/common-service';
import { ApiResponse, SelectItem } from '../../shared/types/types';
import { SettingsService } from '../../shared/service/settings-service';
import { firstValueFrom } from 'rxjs';
import { NotificationService } from '../../shared/service/notification-service';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-update-system-currency-dialog',
  styleUrl: './update-system-currency-dialog.scss',
  templateUrl: './update-system-currency-dialog.html',
})
export class UpdateSystemCurrencyDialog {
  protected readonly dialogRef = inject(MatDialogRef<this>);
  protected readonly data = inject<any>(MAT_DIALOG_DATA);
  protected readonly _cs = inject(CommonService);
  private readonly _ss = inject(SettingsService);
  private readonly _ns = inject(NotificationService);

  protected readonly currencies = signal<SelectItem[]>([]);

  protected readonly title = 'Update System Currency';
  protected form!: FormGroup;

  constructor() {
    this.fetchCurrencies();
    this.form = new FormGroup({
      currency: new FormControl<number | string>(this.data.rowData.referenceId, {
        nonNullable: true,
        validators: [Validators.required],
      }),
    });
  }

  fetchCurrencies(): void {
    firstValueFrom(this._ss.fetchCurrencies()).then((resp: SelectItem[]) => {
      this.currencies.set(resp);
    });
  }

  save(): void {
    const systemConfigDto = this.data.rowData;
    systemConfigDto['referenceId'] = this.form.value.currency;
    firstValueFrom(this._ss.saveSystemConfig(systemConfigDto)).then((resp: ApiResponse) => {
      if (resp.success) {
        this.dialogRef.close(resp.message);
      } else {
        this._ns.error(resp.message);
      }
    });
  }
}
