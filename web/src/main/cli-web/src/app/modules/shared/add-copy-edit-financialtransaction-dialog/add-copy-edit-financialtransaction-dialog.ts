import { Component, inject, signal } from '@angular/core';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { CommonService } from '../../shared/service/common-service';
import { firstValueFrom } from 'rxjs';
import { ApiResponse, FinancialTransactionDto, SelectItem } from '../../shared/types/types';
import { SettingsService } from '../../shared/service/settings-service';
import { DATA_FIELDS, MODES, REF_OBJ_NAMES, TRANSACTION_TYPES } from '../../shared/enums';
import { EMPTY } from '../../shared/constants';
import { FinancialTransactionsService } from '../../shared/service/financial-transactions-service';
import { NotificationService } from '../../shared/service/notification-service';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-copy-add-edit-financialtransaction-dialog',
  styleUrl: './add-copy-edit-financialtransaction-dialog.scss',
  templateUrl: './add-copy-edit-financialtransaction-dialog.html',
})
export class AddCopyEditFinancialTransactionDialog {
  protected readonly data = inject<any>(MAT_DIALOG_DATA);
  protected readonly dialogRef = inject(MatDialogRef<AddCopyEditFinancialTransactionDialog>);
  private readonly _cms = inject(CategoryManagementService);
  public readonly _cs = inject(CommonService);
  private readonly _ss = inject(SettingsService);
  private readonly _fts = inject(FinancialTransactionsService);
  private readonly _ns = inject(NotificationService);

  protected readonly transactionTypes = signal<SelectItem[]>([]);
  protected readonly categories = signal<SelectItem[]>([]);
  protected readonly subcategories = signal<SelectItem[]>([]);
  protected readonly locations = signal<SelectItem[]>([]);

  protected readonly maxDate = new Date();
  protected form!: FormGroup;
  protected readonly transactionType!: any;
  protected transactionTypeId!: any;

  constructor() {
    this.transactionType = this.data.transactionType;
    this.fetchLocations();
    this.fetchdropdowns();
    this.createForm();
  }

  fetchdropdowns() {
    if (this.data.allTransactions) {
      this.fetchTransactionTypes();
    } else {
      this.fetchCategories(this.transactionType, false);
      this.fetchTransactionTypeId();
    }
    if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
      this.fetchSubcategories(this.data.selectedRow.categoryId, false);
    }
  }

  fetchTransactionTypes(): void {
    firstValueFrom(this._ss.fetchTransactionTypes()).then((resp: SelectItem[]) => {
      this.transactionTypes.set(resp);
      if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
        this.fetchCategories(this.data.selectedRow.transactionTypeId, false);
      }
    });
  }

  fetchCategories(transactionTypeId: any, clearValue: boolean = true): void {
    if (clearValue) {
      this.form.controls['category'].setValue('');
    }
    const transactionType = this.transactionTypes().filter((e) => e.value === transactionTypeId)[0]
      ?.label;
    firstValueFrom(
      this._cms.fetchCategoriesByReferenceCode(
        transactionType ? transactionType : this.transactionType,
      ),
    ).then((resp: SelectItem[]) => {
      this.categories.set(resp);
    });
  }

  fetchTransactionTypeId(): void {
    firstValueFrom(
      this._ss.fetchIdByReferenceCodeAndRefObjName(
        this.data.transactionType,
        REF_OBJ_NAMES.TRANSACTION_TYPE,
      ),
    ).then((resp: any) => {
      this.transactionTypeId = resp;
    });
  }

  fetchLocations(): void {
    firstValueFrom(this._ss.fetchLocations()).then((resp: SelectItem[]) => {
      this.locations.set(resp);
    });
  }

  fetchSubcategories(categoryId: any, clearValue: boolean = true): void {
    if (clearValue) {
      this.form.controls['subcategory'].setValue('');
    }
    firstValueFrom(this._cms.fetchSubcategoriesByCategory(categoryId)).then(
      (resp: SelectItem[]) => {
        this.subcategories.set(resp);
      },
    );
  }

  getDateValue(fieldName: string): Date {
    return this.data.mode === MODES.ADD ? new Date() : new Date(this.data.selectedRow[fieldName]);
  }

  getValue(fieldName: string): any {
    return this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow[fieldName];
  }

  createForm(): void {
    this.form = new FormGroup({
      transactionDate: new FormControl<Date>(this.getDateValue(DATA_FIELDS.TRANSACTION_AT), {
        nonNullable: true,
        validators: [Validators.required],
      }),
      amount: new FormControl<string>(this.getValue(DATA_FIELDS.AMOUNT), {
        nonNullable: true,
        validators: [Validators.required],
      }),
      transactionType: new FormControl<number | string>(
        this.getValue(DATA_FIELDS.TRANSACTION_TYPE_ID),
        {
          nonNullable: true,
          validators: this.data.allTransactions ? [Validators.required] : [],
        },
      ),
      category: new FormControl<number | string>(this.getValue(DATA_FIELDS.CATEGORY_ID), {
        nonNullable: true,
        validators: [Validators.required],
      }),
      subcategory: new FormControl<number | string>(this.getValue(DATA_FIELDS.SUBCATEGORY_ID), {
        nonNullable: true,
        validators: [Validators.required],
      }),
      location: new FormControl<number | string>(this.getValue(DATA_FIELDS.LOCATION_ID), {
        nonNullable: true,
      }),
      remarks: new FormControl<string>(this.getValue(DATA_FIELDS.REMARKS), {
        nonNullable: true,
      }),
    });
  }

  save() {
    const transactionDate = this.form.value.transactionDate;
    const transactionAt =
      typeof transactionDate === 'string'
        ? transactionDate
        : this._cs.formatDateOnly(transactionDate);

    const transactionType = this.data.allTransactions
      ? this.transactionTypes().find((e) => e.value === this.form.value.transactionType)?.label
      : this.transactionType;

    const amount =
      transactionType === TRANSACTION_TYPES.EXPENSE
        ? -Math.abs(this.form.value.amount)
        : Math.abs(this.form.value.amount);

    const financialTransactionDto: FinancialTransactionDto = {
      transactionAt: transactionAt,
      amount: amount,
      categoryId: this.form.value.category,
      subcategoryId: this.form.value.subcategory,
      locationId: this.form.value.location,
      remarks: this.form.value.remarks,
      transactionTypeId: this.data.allTransactions
        ? this.form.value.transactionType
        : this.transactionTypeId,
      id: this.data.mode !== MODES.EDIT ? null : this.data.selectedRow.id,
    };
    firstValueFrom(this._fts.saveFinancialTransaction(financialTransactionDto)).then(
      (resp: ApiResponse) => {
        if (resp.success) {
          this.dialogRef.close(resp.message);
        } else {
          this._ns.error(resp.message);
        }
      },
    );
  }
}
