import { Component, inject } from '@angular/core';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { CommonService } from '../../shared/service/common-service';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { SelectItem } from '../../shared/types/types';
import { SettingsService } from '../../shared/service/settings-service';
import { CATEGORY_TYPES, FORM_CONTROLES, MODES } from '../../shared/enums';
import { EMPTY } from '../../shared/constants';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-add-edit-expense-dialog',
  styleUrl: './add-edit-expense-dialog.scss',
  templateUrl: './add-edit-expense-dialog.html',
})
export class AddEditExpenseDialog {
  protected readonly data = inject<any>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<AddEditExpenseDialog>);
  private _cms = inject(CategoryManagementService);
  public _cs = inject(CommonService);
  private _ss = inject(SettingsService);
  protected categoriesSubject = new BehaviorSubject<SelectItem[]>([]);
  protected categories$ = this.categoriesSubject.asObservable();
  protected subcategoriesSubject = new BehaviorSubject<SelectItem[]>([]);
  protected subcategories$ = this.subcategoriesSubject.asObservable();
  protected locationsSubject = new BehaviorSubject<SelectItem[]>([]);
  protected locations$ = this.locationsSubject.asObservable();
  maxDate = new Date();
  form: FormGroup = new FormGroup({});

  constructor() {
    this.fetchCategories();
    this.fetchLocations();
    this.createForm();
  }

  fetchCategories() {
    firstValueFrom(this._cms.fetchCategoriesByReferenceCode(CATEGORY_TYPES.EXPENSE)).then(
      (resp: SelectItem[]) => {
        this.categoriesSubject.next(resp);
      },
    );
  }

  fetchLocations() {
    firstValueFrom(this._ss.fetchLocations()).then((resp: SelectItem[]) => {
      this.locationsSubject.next(resp);
    });
  }

  fetchSubcategories(categoryId: any) {
    this.form.controls[FORM_CONTROLES.SUBCATEGORY].setValue('');
    firstValueFrom(this._cms.fetchSubcategoriesByCategory(categoryId)).then(
      (resp: SelectItem[]) => {
        this.subcategoriesSubject.next(resp);
      },
    );
  }

  createForm() {
    this.form.addControl(
      FORM_CONTROLES.EXPENSE_DATE,
      new FormControl(this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.transactionAt, [
        Validators.required,
      ]),
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
      new FormControl(this.data.mode === MODES.ADD ? EMPTY : this.data.selectedRow.locationId, [
        Validators.required,
      ]),
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

  save() {}
}
