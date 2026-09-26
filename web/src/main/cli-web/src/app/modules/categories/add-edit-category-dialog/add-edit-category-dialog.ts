import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonService } from '../../shared/service/common-service';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { firstValueFrom } from 'rxjs';
import { NotificationService } from '../../shared/service/notification-service';
import { DATA_FIELDS, MODES } from '../../shared/enums';
import { ApiResponse, SelectItem } from '../../shared/types/types';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { SettingsService } from '../../shared/service/settings-service';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-add-edit-category-dialog',
  styleUrl: './add-edit-category-dialog.scss',
  templateUrl: './add-edit-category-dialog.html',
})
export class AddEditCategoryDialog {
  protected readonly dialogRef = inject(MatDialogRef<AddEditCategoryDialog>);
  protected readonly data = inject<any>(MAT_DIALOG_DATA);
  public readonly _cs = inject(CommonService);
  private readonly _cms = inject(CategoryManagementService);
  private readonly _ss = inject(SettingsService);
  private readonly _ns = inject(NotificationService);

  public transactionTypes = signal<SelectItem[]>([]);

  protected form!: FormGroup;
  protected MODES = MODES;

  constructor() {
    this.fetchTransactionTypes();
    this.createForm();
  }

  fetchTransactionTypes(): void {
    firstValueFrom(this._ss.fetchTransactionTypes()).then((res: SelectItem[]) => {
      this.transactionTypes.set(res);
    });
  }

  getEditValue(fieldName: string): any {
    return this.data.mode === MODES.EDIT ? this.data.selectedRow[fieldName] : '';
  }

  createForm(): void {
    this.form = new FormGroup({
      transactionType: new FormControl<number | string>(
        this.getEditValue(DATA_FIELDS.transaction_type_ID),
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      categoryName: new FormControl<string>(this.getEditValue(DATA_FIELDS.CATEGORY_NAME), {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
      }),
      categoryDescription: new FormControl<string>(
        this.getEditValue(DATA_FIELDS.CATEGORY_DESCRIPTION),
        {
          nonNullable: true,
          validators: [Validators.required, Validators.minLength(3), Validators.maxLength(200)],
        },
      ),
    });
  }

  save(): void {
    const category: any = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : null,
      transactionTypeId: this.form.value.transactionType,
      categoryName: this.form.value.categoryName.trim(),
      categoryDescription: this.form.value.categoryDescription.trim(),
    };
    firstValueFrom(this._cms.saveCategory(category)).then((response: ApiResponse) => {
      console.log(response.success + ' : ' + response.message);
      if (response.success) {
        this.dialogRef.close(response.message);
      } else {
        this._ns.error(response.message);
      }
    });
  }
}
