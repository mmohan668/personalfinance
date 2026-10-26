import { ChangeDetectionStrategy, Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '../shared/service/notification-service';
import {
  DATA_FIELDS,
  FILTER_OPERATORS,
  GRID_EXPORT_FILE_NAMES,
  GRID_NAMES,
  MODES,
} from '../shared/enums';
import { ApiResponse, GridColumn, GridFilter, ToolbarConfig } from '../shared/types/types';
import { MessageService } from '../shared/service/message-service';
import { AddEditUserDialog } from './add-edit-user-dialog/add-edit-user-dialog';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { firstValueFrom } from 'rxjs';
import { UserService } from '../shared/service/user-service';

@Component({
  imports: [DataGrid],
  selector: 'app-users',
  styleUrl: './users.scss',
  templateUrl: './users.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Users {
  @ViewChild('dataGrid') private dataGrid!: DataGrid;

  public readonly _cs = inject(CommonService);
  protected readonly dialog = inject(MatDialog);
  protected readonly _ns = inject(NotificationService);
  protected readonly _ms = inject(MessageService);
  protected readonly _us = inject(UserService);

  protected readonly gridName = GRID_NAMES.USERS_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.USERS_GRID_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly toolbaConfig!: ToolbarConfig;
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.ADMIN_USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 2 },
  ];

  constructor() {
    this.toolbaConfig = this._cs.toolbarConfig(true);
    this.toolbaConfig.copyRow = false;
  }

  calculateCellValue = (rowData: any, col: GridColumn): any => {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    if (col.field === 'isAdminUser') {
      return rowData[col.field] ? 'Yes' : 'No';
    }
    return rowData[col.field];
  };

  fetchBooleanOptions = (col: GridColumn) => {
    if (col.field === 'active') {
      return this._cs.activeInactiveBooleanOptions;
    }
    if (col.field === 'isAdminUser') {
      return this._cs.yesNoBooleanOptions;
    }
    return this._cs.activeInactiveBooleanOptions;
  };

  addRow = () => {
    this.dialog
      .open(AddEditUserDialog, {
        width: '80vw',
        maxWidth: '80vw',
        data: {
          mode: MODES.ADD,
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((message) => {
        if (message) {
          this._ns.success(message);
        }
        this.dataGrid.refreshGrid();
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
    if (this.dataGrid.selectedRows.at(0).isAdminUser === true) {
      this._ns.error(this._ms.get('user.edit.isAdminUser'));
      return;
    }
    this.dialog
      .open(AddEditUserDialog, {
        width: '80vw',
        maxWidth: '80vw',
        data: {
          mode: MODES.EDIT,
          selectedRow: this.dataGrid.selectedRows.at(0),
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((message) => {
        if (message) {
          this._ns.success(message);
        }
        this.dataGrid.refreshGrid();
      });
  };

  deleteRow = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    if (
      this.dataGrid.selectedRows.length === 1 &&
      this.dataGrid.selectedRows.at(0).isAdminUser === true
    ) {
      this._ns.error(this._ms.get('user.delete.isAdminUser'));
      return;
    }
    const title = this._ms.get('user.delete.title');
    const message = this._ms.get('user.delete.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: { title: title, message: message },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid?.selectedRows
            .filter((row) => row.isAdminUser === false)
            .map((row) => row.id);
          firstValueFrom(this._us.deleteUsers(ids)).then((resp: ApiResponse) => {
            if (resp.success) {
              this._ns.success(resp.message);
              this.dataGrid?.refreshGrid();
            } else {
              this._ns.error(resp.message);
            }
          });
        }
      });
  };

  activate = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.activate.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.every((user) => user.active === true)) {
      this._ns.error(this._ms.get('user.activate.allactive'));
      return;
    }
    const title = this._ms.get('user.activate.title');
    const message = this.dataGrid?.selectedRows.some((row) => row.active === true)
      ? this._ms.get('user.activate.mixedSelection')
      : this._ms.get('user.activate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: { title: title, message: message },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid?.selectedRows
            .filter((row) => row.active === false)
            .map((row) => row.id);
          firstValueFrom(this._us.activateUsers(ids)).then((resp: ApiResponse) => {
            if (resp.success) {
              this._ns.success(resp.message);
              this.dataGrid?.refreshGrid();
            } else {
              this._ns.error(resp.message);
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
    if (
      this.dataGrid.selectedRows.length === 1 &&
      this.dataGrid.selectedRows.at(0).isAdminUser === true
    ) {
      this._ns.error(this._ms.get('user.inactivate.isAdminUser'));
      return;
    }
    if (this.dataGrid.selectedRows.every((user) => user.active === false)) {
      this._ns.error(this._ms.get('user.inactivate.allinactive'));
      return;
    }
    const title = this._ms.get('user.inactivate.title');
    const message = this.dataGrid?.selectedRows.some((row) => row.active === false)
      ? this._ms.get('user.inactivate.mixedSelection')
      : this._ms.get('user.inactivate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: { title: title, message: message },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid?.selectedRows
            .filter((row) => row.active === true && row.isAdminUser === false)
            .map((row) => row.id);
          firstValueFrom(this._us.inactivateUsers(ids)).then((resp: ApiResponse) => {
            if (resp.success) {
              this._ns.success(resp.message);
              this.dataGrid?.refreshGrid();
            } else {
              this._ns.error(resp.message);
            }
          });
        }
      });
  };
}
