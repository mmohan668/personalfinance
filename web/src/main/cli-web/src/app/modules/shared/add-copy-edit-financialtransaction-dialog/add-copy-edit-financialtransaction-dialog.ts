import { Component, inject } from '@angular/core';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { CommonService } from '../../shared/service/common-service';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { ApiResponse, FinancialTransactionDto, SelectItem } from '../../shared/types/types';
import { SettingsService } from '../../shared/service/settings-service';
import { FORM_CONTROLES, MODES, REF_OBJ_NAMES } from '../../shared/enums';
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
  protected categoriesSubject = new BehaviorSubject<SelectItem[]>([]);
  protected categories$ = this.categoriesSubject.asObservable();
  protected subcategoriesSubject = new BehaviorSubject<SelectItem[]>([]);
  protected subcategories$ = this.subcategoriesSubject.asObservable();
  protected locationsSubject = new BehaviorSubject<SelectItem[]>([]);
  protected locations$ = this.locationsSubject.asObservable();
  maxDate = new Date();
  form: FormGroup = new FormGroup({});
  categoryTypeId!: any;

  constructor() {
    this.fetchCategories();
    this.fetchLocations();
    this.createForm();
    this.fetchCategoryTypeId();
    if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
      this.fetchSubcategories(this.data.selectedRow.categoryId, false);
    }
  }

  fetchCategories() {
    firstValueFrom(this._cms.fetchCategoriesByReferenceCode(this.data.categoryType)).then(
      (resp: SelectItem[]) => {
        this.categoriesSubject.next(resp);
      },
    );
  }

  fetchCategoryTypeId() {
    firstValueFrom(
      this._ss.fetchIdByReferenceCodeAndRefObjName(
        this.data.categoryType,
        REF_OBJ_NAMES.CATEGORY_TYPE,
      ),
    ).then((resp: any) => {
      this.categoryTypeId = resp;
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
      this.form.reset();
    }
  }

  save() {
    const transactionDate = this.form.value.transactionDate;
    const transactionAt =
      typeof transactionDate === 'string'
        ? transactionDate
        : this._cs.formatDateOnly(transactionDate);
    const financialTransactionDto: FinancialTransactionDto = {
      transactionAt: transactionAt,
      amount: this.form.value.amount,
      categoryId: this.form.value.category,
      subcategoryId: this.form.value.subcategory,
      locationId: this.form.value.location,
      remarks: this.form.value.remarks,
      transactionTypeId: this.categoryTypeId,
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
