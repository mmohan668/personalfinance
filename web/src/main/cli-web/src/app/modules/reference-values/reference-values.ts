import { Component, inject, ViewChild } from '@angular/core';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { SelectItem, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { DataGrid } from '../shared/data-grid/data-grid';
import { MatDialog } from '@angular/material/dialog';
import { AdEditReferenceValueDialog } from './ad-edit-reference-value-dialog/ad-edit-reference-value-dialog';
import { SettingsService } from '../shared/service/settings-service';
import { firstValueFrom } from 'rxjs';
import { NotificationService } from '../shared/service/notification-service';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';

@Component({
  imports: [DataGrid],
  selector: 'app-reference-values',
  styleUrl: './reference-values.scss',
  templateUrl: './reference-values.html',
})
export class ReferenceValues {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  protected readonly gridName = GRID_NAMES.REFERENCE_VALUES_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_VALUES_EFN;
  toolbarConfig!: ToolbarConfig;
  private _cs = inject(CommonService);
  private dialog = inject(MatDialog);
  private _ss = inject(SettingsService);
  private _ns = inject(NotificationService);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig;
    this.toolbarConfig.addRow = true;
    this.toolbarConfig.deleteRow = true;
  }

  addRow = async () => {
    firstValueFrom(this._ss.fetchCategoryTypes())
      .then((res: SelectItem[]) => {
        const categoryTypes: SelectItem[] = res;
        this.dialog
          .open(AdEditReferenceValueDialog, {
            width: '70vw',
            maxWidth: '70vw',
            data: {
              mode: 'Add',
              categoryTypes: categoryTypes,
            },
            disableClose: true,
          })
          .afterClosed()
          .subscribe(() => {
            this.dataGrid?.refreshGrid();
          });
      })
      .catch((error) => {
        console.log('Error while fetchCategoryTypes:', error);
      });
  };

  deleteRow = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error('Please select at least one record to delete.');
      return;
    }
    if (this.dataGrid.selectedRows.every((row) => row.createdBy === 'SYSTEM')) {
      this._ns.error('System-generated reference values cannot be deleted.');
      return;
    }
    const title = 'Delete Confirmation';
    const message = this.dataGrid.selectedRows.some((row) => row.createdBy === 'SYSTEM')
      ? 'Some of the selected reference values are system-generated and cannot be deleted. Only the user-created reference values will be deleted. Do you want to continue?'
      : 'Are you sure you want to delete the selected reference values?';
    this.dialog
      .open(ConfirmationDialog, {
        width: '70vm',
        data: {
          title: title,
          message: message,
          isNotification: false,
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid.selectedRows
            .filter((row) => row.createdBy !== 'SYSTEM')
            .map((row) => row.id);
          firstValueFrom(this._ss.deleteReferenceValue(ids))
            .then((response) => {
              if (response.success) {
                this._ns.success(response.message);
                this.dataGrid.refreshGrid();
              } else {
                this._ns.error(response.message);
              }
            })
            .catch((error) => {
              console.log('Error while deleteReferenceValue:', error);
            });
        }
      });
  };
}
