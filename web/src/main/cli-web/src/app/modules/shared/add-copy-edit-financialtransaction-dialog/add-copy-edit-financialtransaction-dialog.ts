import { Component, inject } from '@angular/core';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { CommonService } from '../../shared/service/common-service';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { ApiResponse, FinancialTransactionDto, SelectItem } from '../../shared/types/types';
import { SettingsService } from '../../shared/service/settings-service';
import { FORM_CONTROLES, MODES, REF_OBJ_NAMES, TRANSACTION_TYPES } from '../../shared/enums';
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
  private dialogRef = inject(MatDialogRef<AddCopyEditFinancialTransactionDialog>);
  private _cms = inject(CategoryManagementService);
  public _cs = inject(CommonService);
  private _ss = inject(SettingsService);
  private _fts = inject(FinancialTransactionsService);
  private _ns = inject(NotificationService);
  protected transactionTypesSubject = new BehaviorSubject<SelectItem[]>([]);
  protected transactionTypes$ = this.transactionTypesSubject.asObservable();
  protected categoriesSubject = new BehaviorSubject<SelectItem[]>([]);
  protected categories$ = this.categoriesSubject.asObservable();
  protected subcategoriesSubject = new BehaviorSubject<SelectItem[]>([]);
  protected subcategories$ = this.subcategoriesSubject.asObservable();
  protected locationsSubject = new BehaviorSubject<SelectItem[]>([]);
  protected locations$ = this.locationsSubject.asObservable();
  maxDate = new Date();
  form: FormGroup = new FormGroup({});
  transactionType!: any;
  transactionTypeId!: any;

  constructor() {
    this.transactionType = this.data.transactionType;
    this.fetchLocations();
    this.createForm();
    if (this.data.allTransactions) {
      this.fetchTransactionTypes();
    } else {
      this.fetchCategories(this.transactionType, false);
      this.fetchCategoryTypeId();
    }
    if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
      this.fetchSubcategories(this.data.selectedRow.categoryId, false);
    }
  }

  fetchTransactionTypes() {
    firstValueFrom(this._ss.fetchCategoryTypes()).then((resp: SelectItem[]) => {
      this.transactionTypesSubject.next(resp);
      if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
        this.fetchCategories(this.data.selectedRow.transactionTypeId, false);
      }
    });
  }

  fetchCategories(transactionTypeId: any, clearValue: boolean = true) {
    if (clearValue) {
      this.form.controls[FORM_CONTROLES.CATEGORY].setValue('');
    }
    const transactionType = this.transactionTypesSubject.value?.filter(
      (e) => e.value === transactionTypeId,
    )[0]?.label;
    firstValueFrom(
      this._cms.fetchCategoriesByReferenceCode(
        transactionType ? transactionType : this.transactionType,
      ),
    ).then((resp: SelectItem[]) => {
      this.categoriesSubject.next(resp);
    });
  }

  fetchCategoryTypeId() {
    firstValueFrom(
      this._ss.fetchIdByReferenceCodeAndRefObjName(
        this.data.transactionType,
        REF_OBJ_NAMES.CATEGORY_TYPE,
      ),
    ).then((resp: any) => {
      this.transactionTypeId = resp;
    });
  }

  fetchLocations() {
    firstValueFrom(this._ss.fetchLocations()).then((resp: SelectItem[]) => {
      this.locationsSubject.next(resp);
    });
  }

  fetchSubcategories(categoryId: any, clearValue: boolean = true) {
    if (clearValue) {
      this.form.controls[FORM_CONTROLES.SUBCATEGORY].setValue('');
    }
    firstValueFrom(this._cms.fetchSubcategoriesByCategory(categoryId)).then(
      (resp: SelectItem[]) => {
        this.subcategoriesSubject.next(resp);
      },
    );
  }

  createForm() {
    this.form.addControl(
      FORM_CONTROLES.TRANSACTION_DATE,
      new FormControl(
        this.data.mode === MODES.ADD ? new Date() : this.data.selectedRow.transactionAt,
        [Validators.required],
      ),
    );
    this.form.addControl(
      FORM_CONTROLES.AMOUNT,
      new FormControl(this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.amount, [
        Validators.required,
      ]),
    );
    if (this.data.allTransactions) {
      this.form.addControl(
        FORM_CONTROLES.TRANSACTION_TYPE,
        new FormControl(
          this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.transactionTypeId,
          [Validators.required],
        ),
      );
    }
    this.form.addControl(
      FORM_CONTROLES.CATEGORY,
      new FormControl(this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.categoryId, [
        Validators.required,
      ]),
    );
    this.form.addControl(
      FORM_CONTROLES.SUBCATEGORY,
      new FormControl(this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.subcategoryId, [
        Validators.required,
      ]),
    );
    this.form.addControl(
      FORM_CONTROLES.LOCATION,
      new FormControl(this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.locationId),
    );
    this.form.addControl(
      FORM_CONTROLES.REMARKS,
      new FormControl(this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.remarks),
    );
  }

  close() {
    this.dialogRef.close();
  }

  clear() {
    if (this.data.mode === MODES.ADD) {
      this.form.reset({ transactionDate: new Date() });
    } else {
      this.form.reset(
        this.data.allTransactions
          ? {
              transactionDate: this.data.selectedRow.transactionAt,
              amount: this.data.selectedRow.amount,
              transactionType: this.data.selectedRow.transactionTypeId,
              category: this.data.selectedRow.categoryId,
              subcategory: this.data.selectedRow.subcategoryId,
              location: this.data.selectedRow.locationId,
              remarks: this.data.selectedRow.remarks,
            }
          : {
              transactionDate: this.data.selectedRow.transactionAt,
              amount: this.data.selectedRow.amount,
              category: this.data.selectedRow.categoryId,
              subcategory: this.data.selectedRow.subcategoryId,
              location: this.data.selectedRow.locationId,
              remarks: this.data.selectedRow.remarks,
            },
      );
    }
  }

  save() {
    const transactionDate = this.form.value.transactionDate;
    const transactionAt =
      typeof transactionDate === 'string'
        ? transactionDate
        : this._cs.formatDateOnly(transactionDate);

    const transactionType = this.data.allTransactions
      ? this.transactionTypesSubject.value.find((e) => e.value === this.form.value.transactionType)
          ?.label
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
