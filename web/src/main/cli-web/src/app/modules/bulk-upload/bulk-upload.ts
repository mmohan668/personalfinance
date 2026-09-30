import { Component, inject, ViewChild } from '@angular/core';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, FILTER_OPERATORS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { ApiResponse, GridColumn, GridFilter, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { BulkUploadService } from '../shared/service/bulk-upload-service';
import { firstValueFrom } from 'rxjs';
import { NotificationService } from '../shared/service/notification-service';
import { MessageService } from '../shared/service/message-service';

@Component({
  imports: [CommonImportsModule, DataGrid],
  selector: 'app-bulk-upload',
  styleUrl: './bulk-upload.scss',
  templateUrl: './bulk-upload.html',
})
export class BulkUpload {
  @ViewChild('dataGrid') dataGrid!: DataGrid;

  protected readonly _cs = inject(CommonService);
  protected readonly _bus = inject(BulkUploadService);
  private readonly _ns = inject(NotificationService);
  private readonly _ms = inject(MessageService);

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
    firstValueFrom(this._bus.downloadTemplate(this.selectedUploadType)).then((response) => {
      const blob = response.body;
      if (!blob) {
        console.error('Empty response received while downloading template');
        return;
      }
      const contentDisposition = response.headers.get('Content-Disposition');
      let fileName = `${this.selectedUploadType}-template.xlsx`;
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="?([^"]+)"?/);
        if (match?.[1]) {
          fileName = match[1];
        }
      }
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = fileName;
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);
      window.URL.revokeObjectURL(url);
    });
    console.log('Download template:', this.selectedUploadType);
  }

  reset(): void {
    this.selectedUploadType = '';
    this.selectedFile = null;
    this.fileError = '';
    return;
  }

  submit(): void {
    if (!this.selectedUploadType || !this.selectedFile) {
      this._ns.error(this._ms.get('common.file.required'));
      return;
    }
    const formData = new FormData();
    const uploadTypeLabel = this.uploadTypes.find((u) => u.value === this.selectedUploadType)
      ?.label as string;
    formData.append('uploadType', this.selectedUploadType);
    formData.append('uploadTypeLabel', uploadTypeLabel);
    formData.append('file', this.selectedFile, this.selectedFile.name);
    console.log('Submitting bulk upload', formData);
    firstValueFrom(this._bus.uploadTemplate(formData)).then((resp: ApiResponse) => {
      if (resp.success) {
        this.reset();
        this._ns.success(resp.message);
        this.dataGrid.refreshGrid();
      } else {
        this._ns.error(resp.message);
      }
    });
  }

  get canSubmit(): boolean {
    return !!this.selectedUploadType && !!this.selectedFile;
  }

  hasValue = (rowData: any, col: GridColumn) => {
    if (col.field === 'uploadedFile') {
      return this._cs.isNotNull(rowData['uploadedFile']);
    } else if (col.field === 'errorFile') {
      return this._cs.isNotNull(rowData['errorFile']);
    }
    return false;
  };

  downloadFile = (rowData: any, col: GridColumn) => {
    let filePath = '';
    if (col.field === 'uploadedFile') {
      filePath = rowData['uploadedFile'];
    } else if (col.field === 'errorFile') {
      filePath = rowData['errorFile'];
    }
    this._bus.downloadFile(filePath).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.getFileName(filePath);
        a.click();
        window.URL.revokeObjectURL(url);
      },
    });
  };

  getFileName(filePath: string): string {
    return filePath.split('\\').pop() || 'download';
  }
}
