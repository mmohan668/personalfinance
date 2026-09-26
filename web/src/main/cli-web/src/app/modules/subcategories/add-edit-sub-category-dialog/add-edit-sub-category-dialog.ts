import { Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DATA_FIELDS, MODES } from '../../shared/enums';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiResponse, SelectItem } from '../../shared/types/types';
import { CommonService } from '../../shared/service/common-service';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { NotificationService } from '../../shared/service/notification-service';
import { SettingsService } from '../../shared/service/settings-service';
import { EMPTY } from '../../shared/constants';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-add-edit-sub-category-dialog',
  styleUrl: './add-edit-sub-category-dialog.scss',
  templateUrl: './add-edit-sub-category-dialog.html',
})
export class AddEditSubCategoryDialog {
  protected dialogRef = inject(MatDialogRef<AddEditSubCategoryDialog>);
  public readonly data = inject<any>(MAT_DIALOG_DATA);
  protected categories = signal<SelectItem[]>([]);
  protected transactionTypes = signal<SelectItem[]>([]);
  public _cs = inject(CommonService);
  private _cms = inject(CategoryManagementService);
  private _ns = inject(NotificationService);
  private _ss = inject(SettingsService);
  protected MODES = MODES;
  protected form!: FormGroup;
  constructor() {
    this.createForm();
    this.fetchTransactionTypes();
    this.fetchdropdowns();
  }

  fetchdropdowns() {
    if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
      this.fetchCategories(this.data.selectedRow.transactionTypeId, false);
    } else {
      this.categories.set([]);
    }
  }

  fetchTransactionTypes() {
    firstValueFrom(this._ss.fetchTransactionTypes()).then((resp: SelectItem[]) => {
      this.transactionTypes.set(resp);
    });
  }

  fetchCategories(referenceValueId: any, clearValue: boolean = true) {
    if (clearValue) {
      this.form.controls['category'].setValue('');
    }
    firstValueFrom(this._cms.fetchCategories(referenceValueId)).then((resp: SelectItem[]) => {
      this.categories.set(resp);
    });
  }

  getValue(fieldName: DATA_FIELDS) {
    if ([DATA_FIELDS.transaction_type_ID, DATA_FIELDS.USER_CATEGORY_ID].includes(fieldName)) {
      return this.data.mode === MODES.EDIT || this.data.mode === MODES.COPY
        ? this.data.selectedRow[fieldName]
        : EMPTY;
    }
    return this.data.mode === MODES.EDIT ? this.data.selectedRow[fieldName] : EMPTY;
  }

  createForm() {
    this.form = new FormGroup({
      transactionType: new FormControl<number | string>(this.getValue(DATA_FIELDS.transaction_type_ID), {
        nonNullable: true,
        validators: [Validators.required],
      }),
      category: new FormControl<number | string>(this.getValue(DATA_FIELDS.USER_CATEGORY_ID), {
        nonNullable: true,
        validators: [Validators.required],
      }),
      subcategoryName: new FormControl<string>(this.getValue(DATA_FIELDS.SUBCATEGORY_NAME), {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
      }),
      subcategoryDescription: new FormControl<string>(
        this.getValue(DATA_FIELDS.SUBCATEGORY_DESCRIPTION),
        {
          nonNullable: true,
          validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
        },
      ),
    });
    console.log(this.form);
  }

  save() {
    const subcategory: any = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : null,
      transactionTypeId: this.form.value.transactionTypeId,
      userCategoryId: this.form.value.category,
      subcategoryName: this.form.value.subcategoryName,
      subcategoryDescription: this.form.value.subcategoryDescription,
    };
    firstValueFrom(this._cms.saveSubcategory(subcategory)).then((resp: ApiResponse) => {
      if (resp.success) {
        this._ns.success(resp.message);
        this.dialogRef.close(true);
      } else {
        this._ns.error(resp.message);
      }
    });
  }
}
