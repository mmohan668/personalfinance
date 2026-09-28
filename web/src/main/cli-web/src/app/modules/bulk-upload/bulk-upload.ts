import { Component, inject } from '@angular/core';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, FILTER_OPERATORS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { GridFilter, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';

@Component({
  imports: [CommonImportsModule, DataGrid],
  selector: 'app-bulk-upload',
  styleUrl: './bulk-upload.scss',
  templateUrl: './bulk-upload.html',
})
export class BulkUpload {
  protected readonly _cs = inject(CommonService);

  protected readonly gridName = GRID_NAMES.BULK_UPLOADS_STATUS;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.BULK_UPLOADS_STATUS;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly toolbaConfig!: ToolbarConfig;
  protected readonly uploadTypes = [
    { value: 'reference-values', label: 'Reference Values' },
    { value: 'categories', label: 'Categories' },
    { value: 'subcategories', label: 'Subcategories' },
    { value: 'financial-transactions', label: 'Financial Transactions' },
  ];
  protected selectedUploadType = '';
  protected selectedFile: File | null = null;
  protected fileError = '';
  protected readonly additionalFilters: GridFilter[] = [
    {
      field: DATA_FIELDS.USER_ID,
      operator: FILTER_OPERATORS.EQUALS,
      value: 2,
    },
  ];
  constructor() {
    this.toolbaConfig = this._cs.toolbarConfig();
  }

  onFileSelected(event: Event): void {
    this.fileError = '';
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      this.selectedFile = null;
      return;
    }

    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      this.selectedFile = null;
      this.fileError = 'Only .xlsx files are allowed.';
      input.value = '';
      return;
    }

    this.selectedFile = file;
  }

  downloadTemplate(): void {
    if (!this.selectedUploadType) {
      return;
    }

    // TODO: Replace with API call to download
    // the template for the selected upload type.
    console.log('Download template:', this.selectedUploadType);
  }

  reset(): void {
    this.selectedUploadType = '';
    this.selectedFile = null;
    this.fileError = '';
  }

  submit(): void {
    if (!this.selectedUploadType || !this.selectedFile) {
      return;
    }

    // TODO: Replace with API call.
    const formData = new FormData();

    formData.append('uploadType', this.selectedUploadType);
    formData.append('file', this.selectedFile);

    console.log('Submitting bulk upload', formData);
  }

  get canSubmit(): boolean {
    return !!this.selectedUploadType && !!this.selectedFile;
  }
}
