import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonService } from '../../shared/service/common-service';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { NotificationService } from '../../shared/service/notification-service';
import { MODES } from '../../shared/enums';
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
  private readonly dialogRef = inject(MatDialogRef<AddEditCategoryDialog>);
  readonly data = inject<any>(MAT_DIALOG_DATA);
  form: FormGroup = new FormGroup({});
  public _cs = inject(CommonService);
  private _cms = inject(CategoryManagementService);
  private _ss = inject(SettingsService);
  public categoryTypesSubject = new BehaviorSubject<SelectItem[]>([]);
  categoryTypes$ = this.categoryTypesSubject.asObservable();
  private _ns = inject(NotificationService);
  MODES = MODES;

  constructor() {
    this.fetchCategoryTypes();
    this.createForm();
  }

  fetchCategoryTypes() {
    firstValueFrom(this._ss.fetchCategoryTypes()).then((res: SelectItem[]) => {
      this.categoryTypesSubject.next(res);
    });
  }

  createForm(): void {
    this.form.addControl(
      'categoryType',
      new FormControl(this.data.mode === MODES.EDIT ? this.data.selectedRow.categoryTypeId : '', [
        Validators.required,
      ]),
    );
    this.form.addControl(
      'categoryName',
      new FormControl(this.data.mode === MODES.EDIT ? this.data.selectedRow.categoryName : '', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
      ]),
    );
    this.form.addControl(
      'categoryDescription',
      new FormControl(
        this.data.mode === MODES.EDIT ? this.data.selectedRow.categoryDescription : '',
        [Validators.required, Validators.minLength(3), Validators.maxLength(200)],
      ),
    );
  }

  save(): void {
    const category: any = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : null,
      categoryTypeId: this.form.value.categoryType,
      categoryName: this.form.value.categoryName.trim(),
      categoryDescription: this.form.value.categoryDescription.trim(),
    };
    firstValueFrom(this._cms.saveCategory(category)).then((response: ApiResponse) => {
      console.log(response.success + ' : ' + response.message);
      if (response.success) {
        this._ns.success(response.message);
        this.dialogRef.close(true);
      } else {
        this._ns.error(response.message);
      }
    });
  }

  clear(): void {
    this._ns.close();
    if (this.data.mode === MODES.EDIT) {
      this.form.patchValue({
        categoryType: this.data.selectedRow.categoryTypeId,
        categoryName: this.data.selectedRow.categoryName,
        categoryDescription: this.data.selectedRow.categoryDescription,
      });
    } else {
      this.form.reset();
    }
  }

  close(): void {
    this.dialogRef.close();
  }
}
