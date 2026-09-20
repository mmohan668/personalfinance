import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MODES } from '../../shared/enums';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { ApiResponse, SelectItem } from '../../shared/types/types';
import { CommonService } from '../../shared/service/common-service';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { NotificationService } from '../../shared/service/notification-service';
import { SettingsService } from '../../shared/service/settings-service';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-add-edit-sub-category-dialog',
  styleUrl: './add-edit-sub-category-dialog.scss',
  templateUrl: './add-edit-sub-category-dialog.html',
})
export class AddEditSubCategoryDialog {
  protected dialogRef = inject(MatDialogRef<AddEditSubCategoryDialog>);
  public readonly data = inject<any>(MAT_DIALOG_DATA);
  public categoriesSubject = new BehaviorSubject<SelectItem[]>([]);
  categories$ = this.categoriesSubject.asObservable();
  public categoryTypesSubject = new BehaviorSubject<SelectItem[]>([]);
  categoryTypes$ = this.categoryTypesSubject.asObservable();
  public _cs = inject(CommonService);
  private _cms = inject(CategoryManagementService);
  private _ns = inject(NotificationService);
  private _ss = inject(SettingsService);
  MODES = MODES;
  form: FormGroup = new FormGroup({});
  constructor() {
    this.fetchCategoryTypes();
    this.createForm();
    if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
      this.fetchCategories(this.data.selectedRow.categoryTypeId);
    }
  }

  fetchCategoryTypes() {
    firstValueFrom(this._ss.fetchCategoryTypes()).then((resp: SelectItem[]) => {
      this.categoryTypesSubject.next(resp);
    });
  }

  fetchCategories(referenceValueId: any) {
    this.form.controls['category'].setValue('');
    firstValueFrom(this._cms.fetchCategories(referenceValueId)).then((resp: SelectItem[]) => {
      this.categoriesSubject.next(resp);
    });
  }

  createForm() {
    this.form.addControl(
      'categoryType',
      new FormControl(
        this.data.mode === MODES.EDIT || this.data.mode === MODES.COPY
          ? this.data.selectedRow.categoryTypeId
          : '',
        [Validators.required],
      ),
    );
    this.form.addControl(
      'category',
      new FormControl(
        this.data.mode === MODES.EDIT || this.data.mode === MODES.COPY
          ? this.data.selectedRow.userCategoryId
          : '',
        [Validators.required],
      ),
    );
    this.form.addControl(
      'subcategoryName',
      new FormControl(this.data.mode === MODES.EDIT ? this.data.selectedRow.subcategoryName : '', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
      ]),
    );
    this.form.addControl(
      'subcategoryDescription',
      new FormControl(
        this.data.mode === MODES.EDIT ? this.data.selectedRow.subcategoryDescription : '',
        [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
      ),
    );
  }

  clear() {
    if ([MODES.COPY, MODES.EDIT].includes(this.data.mode)) {
      this.fetchCategories(this.data.selectedRow.categoryTypeId);
    } else {
      this.categoriesSubject.next([]);
    }
    if (this.data.mode === MODES.ADD) {
      this.form.reset();
    }
    if (this.data.mode === MODES.COPY) {
      this.form.reset({
        categoryType: this.data.selectedRow.categoryTypeId,
        category: this.data.selectedRow.userCategoryId,
      });
    }
    if (this.data.mode === MODES.EDIT) {
      this.form.reset({
        categoryType: this.data.selectedRow.categoryTypeId,
        category: this.data.selectedRow.userCategoryId,
        subcategoryName: this.data.selectedRow.subcategoryName,
        subcategoryDescription: this.data.selectedRow.subcategoryDescription,
      });
    }
  }

  close() {
    this.dialogRef.close();
  }

  save() {
    const subcategory: any = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : null,
      categoryTypeId: this.form.value.categoryType,
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
