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
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';

@Component({
  imports: [CommonImportsModule, DataGrid],
  selector: 'app-bulk-upload',
  styleUrl: './bulk-upload.scss',
  templateUrl: './bulk-upload.html',
})
export class BulkUpload {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  @ViewChild('invoiceDataGrid') invoiceDataGrid!: DataGrid;

  protected readonly _cs = inject(CommonService);
  protected readonly _bus = inject(BulkUploadService);
  private readonly _ns = inject(NotificationService);
  private readonly _ms = inject(MessageService);
  private readonly _dialog = inject(MatDialog);

  protected readonly gridName = GRID_NAMES.BULK_UPLOADS_STATUS;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.BULK_UPLOADS_STATUS;
  protected readonly invoiceGridName = GRID_NAMES.INVOICE_UPLOAD_STATUS;
  protected readonly invoiceGridExportFileName = GRID_EXPORT_FILE_NAMES.INVOICE_UPLOAD_STATUS;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly toolbaConfig!: ToolbarConfig;
  protected readonly invoiceToolbarConfig!: ToolbarConfig;
  protected readonly uploadTypes = [
    { value: 'REFERENCE_VALUES', label: 'Reference Values' },
    { value: 'CATEGORIES', label: 'Categories' },
    { value: 'SUBCATEGORIES', label: 'Subcategories' },
    { value: 'FINANCIAL_TRANSACTIONS', label: 'Financial Transactions' },
    { value: 'INVOICE_UPLOAD', label: 'Invoice Upload' },
  ];
  protected selectedUploadType = '';
  protected selectedFile: File | null = null;
  protected fileError = '';

  constructor() {
    this.toolbaConfig = this._cs.toolbarConfig();
    this.toolbaConfig.refresh = true;
    this.invoiceToolbarConfig = {
      ...this.toolbaConfig,
      deleteRow: CommonService.hasPermissionForUser(
        this._cs.loginUserId,
        'Bulk Upload',
        'Bulk Upload',
        'Upload Invoice',
      ),
    };
  }

  onFileSelected(event: Event): void {
    this.fileError = '';
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      this.selectedFile = null;
      return;
    }

    const allowedExtensions = this.allowedFileExtensions;
    if (!allowedExtensions.some((extension) => file.name.toLowerCase().endsWith(extension))) {
      this.selectedFile = null;
      this.fileError = `Allowed file types: ${allowedExtensions.join(', ')}.`;
      input.value = '';
      return;
    }

    this.selectedFile = file;
  }

  downloadTemplate(): void {
    if (!this.hasUploadPermission) {
      return;
    }
    firstValueFrom(this._bus.downloadTemplate(this.selectedUploadType)).then((response) => {
      const blob = response.body;
      if (!blob) {
        console.error('Empty response received while downloading template');
        return;
      }
      let fileName = `${this.selectedUploadType}_TEMPLATE.xlsx`;
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = fileName;
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);
      window.URL.revokeObjectURL(url);
    });
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
    if (!this.canSubmit) {
      this._ns.error('You do not have permission to perform this upload.');
      return;
    }
    const allowedExtensions = this.allowedFileExtensions;
    if (!allowedExtensions.some((extension) => this.selectedFile!.name.toLowerCase().endsWith(extension))) {
      this._ns.error(`Allowed file types: ${allowedExtensions.join(', ')}.`);
      return;
    }
    const formData = new FormData();
    const uploadTypeLabel = this.uploadTypes.find((u) => u.value === this.selectedUploadType)
      ?.label as string;
    formData.append('uploadType', this.selectedUploadType);
    formData.append('uploadTypeLabel', uploadTypeLabel);
    formData.append('file', this.selectedFile, this.selectedFile.name);
    firstValueFrom(this._bus.uploadTemplate(formData)).then((resp: ApiResponse) => {
      if (resp.success) {
        this.reset();
        this._ns.success(resp.message);
        this.dataGrid.refreshGrid();
        this.invoiceDataGrid.refreshGrid();
      } else {
        this._ns.error(resp.message);
      }
    });
  }

  get canSubmit(): boolean {
    return this.hasUploadPermission && !!this.selectedFile;
  }

  get acceptsFileType(): string {
    return this.allowedFileExtensions.join(',');
  }

  private get allowedFileExtensions(): string[] {
    return this.selectedUploadType === 'INVOICE_UPLOAD' ? ['.pdf', '.png', '.jpg', '.jpeg'] : ['.xlsx'];
  }

  get supportsTemplateDownload(): boolean {
    return !!this.selectedUploadType && this.selectedUploadType !== 'INVOICE_UPLOAD';
  }

  get hasUploadPermission(): boolean {
    const uploadActions: Record<string, string> = {
      REFERENCE_VALUES: 'Upload Reference Values',
      CATEGORIES: 'Upload Categories',
      SUBCATEGORIES: 'Upload Subcategories',
      FINANCIAL_TRANSACTIONS: 'Upload Financial Transactions',
      INVOICE_UPLOAD: 'Upload Invoice',
    };
    const actionName = uploadActions[this.selectedUploadType];
    return (
      !!actionName &&
      CommonService.hasPermissionForUser(
        this._cs.loginUserId,
        'Bulk Upload',
        'Bulk Upload',
        actionName,
      )
    );
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

  deleteInvoiceRows = (): void => {
    const selectedRows = this.invoiceDataGrid?.selectedRows ?? [];
    if (selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    this._dialog.open(ConfirmationDialog, {
      width: 'auto',
      data: {
        title: this._ms.get('common.delete.title'),
        message: 'Deleting the selected invoice upload will also delete all invoice entries from the same file and their imported financial transactions. Continue?',
      },
    }).afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      const ids = selectedRows.map((row) => row.id);
      firstValueFrom(this._bus.deleteInvoiceUploadStatuses(ids)).then((response) => {
        if (response.success) {
          this._ns.success(response.message);
          this.invoiceDataGrid?.refreshGrid();
          this.dataGrid?.refreshGrid();
        } else {
          this._ns.error(response.message);
        }
      });
    });
  };
}
