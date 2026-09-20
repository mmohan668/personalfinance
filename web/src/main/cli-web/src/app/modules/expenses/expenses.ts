import { Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES, MODES } from '../shared/enums';
import { CommonService } from '../shared/service/common-service';
import { ToolbarConfig } from '../shared/types/types';
import { MatDialog } from '@angular/material/dialog';
import { AddEditExpenseDialog } from './add-edit-expense-dialog/add-edit-expense-dialog';
import { NotificationService } from '../shared/service/notification-service';

@Component({
  imports: [DataGrid],
  selector: 'app-expenses',
  styleUrl: './expenses.scss',
  templateUrl: './expenses.html',
})
export class Expenses {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  private dialog = inject(MatDialog);
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridName = GRID_NAMES.EXPENSES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.EXPENSES_GRID_EFN;
  public _cs = inject(CommonService);
  private _ns = inject(NotificationService);
  toolbarConfig!: ToolbarConfig;

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig();
    this.toolbarConfig.addRow = true;
  }

  addRow = () => {
    this.dialog
      .open(AddEditExpenseDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.ADD,
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((value: any) => {
        if (value) {
          this._ns.success(value);
          this.dataGrid?.refreshGrid();
        }
      });
  };
}
