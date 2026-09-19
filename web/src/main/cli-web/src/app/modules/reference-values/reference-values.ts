import { Component, inject, ViewChild } from '@angular/core';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES, MODES } from '../shared/enums';
import { ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { DataGrid } from '../shared/data-grid/data-grid';
import { MatDialog } from '@angular/material/dialog';
import { AdEditReferenceValueDialog } from './ad-edit-reference-value-dialog/ad-edit-reference-value-dialog';
import { SettingsService } from '../shared/service/settings-service';
import { firstValueFrom } from 'rxjs';
import { NotificationService } from '../shared/service/notification-service';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { MessageService } from '../shared/service/message-service';
import { SYSTEM } from '../shared/constants';

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
  protected readonly _ms = inject(MessageService);
  toolbarConfig!: ToolbarConfig;
  private _cs = inject(CommonService);
  private dialog = inject(MatDialog);
  private _ss = inject(SettingsService);
  private _ns = inject(NotificationService);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig();
    this.toolbarConfig.addRow = true;
    this.toolbarConfig.deleteRow = true;
    this.toolbarConfig.editRow = true;
  }

  addRow = () => {
    this.dialog
      .open(AdEditReferenceValueDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.ADD,
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe(() => {
        this.dataGrid?.refreshGrid();
      });
  };

  deleteRow = () => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.every((row) => row.createdBy === SYSTEM)) {
      this._ns.error(this._ms.get('referenceValue.delete.systemOnly'));
      return;
    }
    const title = this._ms.get('common.delete.title');
    const message = this.dataGrid.selectedRows.some((row) => row.createdBy === SYSTEM)
      ? this._ms.get('referenceValue.delete.mixedSelection')
      : this._ms.get('referenceValue.delete.confirmation');
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
          const ids = this.dataGrid.selectedRows
            .filter((row) => row.createdBy !== SYSTEM)
            .map((row) => row.id);
          firstValueFrom(this._ss.deleteReferenceValue(ids)).then((response) => {
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

  editRow = () => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.edit.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.length > 1) {
      this._ns.error(this._ms.get('common.edit.singleSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.every((row) => row.createdBy === SYSTEM)) {
      this._ns.error(this._ms.get('referenceValue.edit.systemOnly'));
      return;
    }

    this.dialog
      .open(AdEditReferenceValueDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.EDIT,
          selectedRow: this.dataGrid.selectedRows[0],
        },
      })
      .afterClosed()
      .subscribe(() => {
        this.dataGrid.refreshGrid();
      });
  };
}
