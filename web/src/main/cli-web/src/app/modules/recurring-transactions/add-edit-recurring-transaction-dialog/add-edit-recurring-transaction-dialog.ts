import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { CategoryManagementService } from '../../shared/service/category-management-service';
import { CommonService } from '../../shared/service/common-service';
import { NotificationService } from '../../shared/service/notification-service';
import { RecurringTransactionRule, RecurringTransactionsService } from '../../shared/service/recurring-transactions-service';
import { SettingsService } from '../../shared/service/settings-service';
import { DATA_FIELDS, MODES, REF_OBJ_NAMES } from '../../shared/enums';
import { ApiResponse, SelectItem } from '../../shared/types/types';

@Component({
  selector: 'app-add-edit-recurring-transaction-dialog',
  imports: [CommonImportsModule],
  templateUrl: './add-edit-recurring-transaction-dialog.html',
})
export class AddEditRecurringTransactionDialog {
  protected readonly data = inject<any>(MAT_DIALOG_DATA);
  protected readonly dialogRef = inject(MatDialogRef<AddEditRecurringTransactionDialog>);
  protected readonly commonService = inject(CommonService);
  private readonly categoryService = inject(CategoryManagementService);
  private readonly recurringService = inject(RecurringTransactionsService);
  private readonly settingsService = inject(SettingsService);
  private readonly notificationService = inject(NotificationService);

  protected readonly transactionTypes = signal<SelectItem[]>([]);
  protected readonly categories = signal<SelectItem[]>([]);
  protected readonly subcategories = signal<SelectItem[]>([]);
  protected readonly locations = signal<SelectItem[]>([]);
  protected readonly form = new FormGroup({
    ruleName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(150), Validators.pattern(/\S/)] }),
    transactionType: new FormControl<number | string>('', Validators.required),
    categoryId: new FormControl<number | null>(null, Validators.required),
    subcategoryId: new FormControl<number | null>(null, Validators.required),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.0001)]),
    locationId: new FormControl<number | null>(null),
    remarks: new FormControl('', { nonNullable: true }),
    frequency: new FormControl('MONTHLY', { nonNullable: true, validators: [Validators.required] }),
    intervalValue: new FormControl(1, { nonNullable: true, validators: [Validators.required, Validators.min(1)] }),
    dayOfWeek: new FormControl(1),
    dayOfMonth: new FormControl(1),
    monthOfYear: new FormControl(1),
    monthlyPattern: new FormControl('DAY_OF_MONTH', { nonNullable: true }),
    startDate: new FormControl(new Date().toISOString().slice(0, 10), { nonNullable: true, validators: [Validators.required] }),
    endDate: new FormControl('', { nonNullable: true }),
    executionTime: new FormControl('06:00', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    firstValueFrom(this.settingsService.fetchReferenceValuesByRefObjName(REF_OBJ_NAMES.TRANSACTION_TYPE))
      .then(async (items) => {
        this.transactionTypes.set(items);
        if (this.data.mode === MODES.EDIT) await this.loadRule(this.data.selectedRow);
      });
    firstValueFrom(this.settingsService.fetchReferenceValuesByRefObjName(REF_OBJ_NAMES.LOCATION))
      .then((items) => this.locations.set(items));
  }

  async onTransactionTypeChange(type: string | number, clearValue = true): Promise<void> {
    if (clearValue) {
      this.form.controls.categoryId.setValue(null);
      this.form.controls.subcategoryId.setValue(null);
    }
    this.subcategories.set([]);
    const typeLabel = this.transactionTypes().find(
      (item) => String(item.value) === String(type),
    )?.label;
    const transactionType = this.commonService.trimDesc(typeLabel ?? String(type));
    this.categories.set(await firstValueFrom(
      this.categoryService.fetchCategoriesByReferenceCode(transactionType),
    ));
  }

  async onCategoryChange(categoryId: number, clearValue = true): Promise<void> {
    if (clearValue) this.form.controls.subcategoryId.setValue(null);
    this.subcategories.set(await firstValueFrom(
      this.categoryService.fetchSubcategoriesByCategory(String(categoryId)),
    ));
  }

  async save(): Promise<void> {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    const typeLabel = this.transactionTypes().find(
      (item) => String(item.value) === String(value.transactionType),
    )?.label;
    const type = this.commonService.trimDesc(typeLabel ?? String(value.transactionType));
    const rule: RecurringTransactionRule = {
      id: this.data.mode === MODES.EDIT ? this.data.selectedRow.id : undefined,
      ruleName: value.ruleName.trim(),
      categoryId: value.categoryId!,
      subcategoryId: value.subcategoryId!,
      amount: type === 'EXPENSE' ? -Math.abs(value.amount!) : Math.abs(value.amount!),
      locationId: value.locationId,
      remarks: value.remarks.trim() || null,
      frequency: value.frequency,
      intervalValue: value.intervalValue,
      dayOfWeek: value.frequency === 'WEEKLY' ? value.dayOfWeek : null,
      dayOfMonth: value.frequency === 'YEARLY' ||
        (value.frequency === 'MONTHLY' && value.monthlyPattern === 'DAY_OF_MONTH')
        ? value.dayOfMonth : null,
      monthOfYear: value.frequency === 'YEARLY' ? value.monthOfYear : null,
      monthlyPattern: value.frequency === 'MONTHLY' ? value.monthlyPattern : null,
      startDate: value.startDate,
      endDate: value.endDate || null,
      executionTime: value.executionTime,
      active: this.data.mode === MODES.EDIT ? this.data.selectedRow.active : true,
    };
    const response: ApiResponse = await firstValueFrom(this.recurringService.saveRule(rule));
    if (response.success) this.dialogRef.close(response.message);
    else this.notificationService.error(response.message);
  }

  async resetForm(): Promise<void> {
    if (this.data.mode === MODES.EDIT) {
      await this.loadRule(this.data.selectedRow);
      return;
    }

    this.form.reset({
      ruleName: '',
      transactionType: '',
      categoryId: null,
      subcategoryId: null,
      amount: null,
      locationId: null,
      remarks: '',
      frequency: 'MONTHLY',
      intervalValue: 1,
      dayOfWeek: 1,
      dayOfMonth: 1,
      monthOfYear: 1,
      monthlyPattern: 'DAY_OF_MONTH',
      startDate: new Date().toISOString().slice(0, 10),
      endDate: '',
      executionTime: '06:00',
    });
    this.categories.set([]);
    this.subcategories.set([]);
  }

  private async loadRule(rule: RecurringTransactionRule): Promise<void> {
    const type = this.transactionTypes().find(
      (item) => this.commonService.trimDesc(item.label) === rule.transactionType,
    );
    this.form.patchValue({
      ruleName: rule.ruleName,
      transactionType: type?.value ?? rule.transactionType ?? '',
      amount: Math.abs(rule.amount),
      locationId: rule.locationId,
      remarks: rule.remarks ?? '',
      frequency: rule.frequency,
      intervalValue: rule.intervalValue,
      dayOfWeek: rule.dayOfWeek,
      dayOfMonth: rule.dayOfMonth,
      monthOfYear: rule.monthOfYear,
      monthlyPattern: rule.monthlyPattern ?? 'DAY_OF_MONTH',
      startDate: rule.startDate,
      endDate: rule.endDate ?? '',
      executionTime: rule.executionTime,
    });
    const transactionType = type?.value ?? rule.transactionType ?? '';
    await this.onTransactionTypeChange(transactionType, false);
    this.form.patchValue({ categoryId: rule.categoryId });
    await this.onCategoryChange(rule.categoryId, false);
    this.form.patchValue({ subcategoryId: rule.subcategoryId });
    this.form.markAsPristine();
  }
}
