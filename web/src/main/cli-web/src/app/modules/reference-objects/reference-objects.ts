import { Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES, MODES } from '../shared/enums';
import { ApiResponse, GridColumn, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { SettingsService } from '../shared/service/settings-service';
import { NotificationService } from '../shared/service/notification-service';
import { MessageService } from '../shared/service/message-service';
import { firstValueFrom } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { AddEditReferenceObjectDialog } from './add-edit-reference-object-dialog/add-edit-reference-object-dialog';

@Component({
  imports: [DataGrid],
  selector: 'app-reference-objects',
  styleUrl: './reference-objects.scss',
  templateUrl: './reference-objects.html',
})
export class ReferenceObjects {
  @ViewChild('dataGrid') dataGrid!: DataGrid;

  private readonly _cs = inject(CommonService);
  private readonly _ns = inject(NotificationService);
  private readonly _ss = inject(SettingsService);
  private readonly _ms = inject(MessageService);
  protected readonly dialog = inject(MatDialog);

  protected readonly gridName = GRID_NAMES.REFERENCE_OBJECTS_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_OBJECTS_EFN;
  protected readonly toolbarConfig!: ToolbarConfig;

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig(true);
    this.toolbarConfig.copyRow = false;
  }

  calculateCellValue = (rowData: any, col: GridColumn): any => {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  };

  activate = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.activate.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.every((row: any) => row.active === true)) {
      this._ns.error(this._ms.get('referenceObject.activate.alreadyActive'));
      return;
    }
    const someActive = this.dataGrid.selectedRows.some((row: any) => row.active === true);
    const title = this._ms.get('common.activate.title');
    const message = someActive
      ? this._ms.get('referenceObject.activate.mixedSelection')
      : this._ms.get('referenceObject.activate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: {
          title: title,
          message: message,
          isNotification: false,
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid.selectedRows.map((row: any) => row[this.dataKey]);
          firstValueFrom(this._ss.activateReferenceObject(ids)).then((response: ApiResponse) => {
            if (response.success) {
              this._ns.success(response.message);
              this.dataGrid.refreshGrid();
            } else {
              this._ns.error(response.message);
            }
          });
        }
      });
  };

  inactivate = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.inactivate.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.every((row: any) => row.active === false)) {
      this._ns.error(this._ms.get('referenceObject.inactivate.alreadyInactive'));
      return;
    }
    const someActive = this.dataGrid.selectedRows.some((row: any) => row.active === false);
    const title = this._ms.get('common.inactivate.title');
    const message = someActive
      ? this._ms.get('referenceObject.inactivate.mixedSelection')
      : this._ms.get('referenceObject.inactivate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: {
          title: title,
          message: message,
          isNotification: false,
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid.selectedRows.map((row: any) => row[this.dataKey]);
          firstValueFrom(this._ss.inactivateReferenceObject(ids)).then((response: ApiResponse) => {
            if (response.success) {
              this._ns.success(response.message);
              this.dataGrid.refreshGrid();
            } else {
              this._ns.error(response.message);
            }
          });
        }
      });
  };

  deleteRow = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    const title = this._ms.get('common.delete.title');
    const message = this._ms.get('referenceObject.delete.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: {
          title: title,
          message: message,
          isNotification: false,
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid.selectedRows.map((row: any) => row[this.dataKey]);
          firstValueFrom(this._ss.deleteReferenceObject(ids)).then((response: ApiResponse) => {
            if (response.success) {
              this._ns.success(response.message);
              this.dataGrid.refreshGrid();
            } else {
              this._ns.error(response.message);
            }
          });
        }
      });
  };

  addRow = () => {
    this.dialog
      .open(AddEditReferenceObjectDialog, {
        width: 'auto',
        data: {
          mode: MODES.ADD,
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((message: string) => {
        if (message) {
          this._ns.success(message);
          this.dataGrid.refreshGrid();
        }
      });
  };

  editRow = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.edit.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.length > 1) {
      this._ns.error(this._ms.get('common.edit.singleSelection'));
      return;
    }
    this.dialog
      .open(AddEditReferenceObjectDialog, {
        width: 'auto',
        data: {
          mode: MODES.EDIT,
          selectedRow: this.dataGrid.selectedRows[0],
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((message: string) => {
        if (message) {
          this._ns.success(message);
          this.dataGrid.refreshGrid();
        }
      });
  };
}
