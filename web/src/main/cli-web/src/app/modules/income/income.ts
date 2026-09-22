import { Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { MatDialog } from '@angular/material/dialog';
import {
  DATA_FIELDS,
  FILTER_OPERATORS,
  GRID_EXPORT_FILE_NAMES,
  GRID_NAMES,
  MODES,
  TRANSACTION_TYPES,
} from '../shared/enums';
import { CommonService } from '../shared/service/common-service';
import { NotificationService } from '../shared/service/notification-service';
import { MessageService } from '../shared/service/message-service';
import { FinancialTransactionsService } from '../shared/service/financial-transactions-service';
import { ApiResponse, GridColumn, GridFilter, ToolbarConfig } from '../shared/types/types';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { firstValueFrom } from 'rxjs';
import { AddCopyEditFinancialTransactionDialog } from '../shared/add-copy-edit-financialtransaction-dialog/add-copy-edit-financialtransaction-dialog';

@Component({
  imports: [DataGrid],
  selector: 'app-income',
  styleUrl: './income.scss',
  templateUrl: './income.html',
})
export class Income {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  private dialog = inject(MatDialog);
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridName = GRID_NAMES.INCOMES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.INCOMES_GRID_EFN;
  public _cs = inject(CommonService);
  private _ns = inject(NotificationService);
  private _ms = inject(MessageService);
  private _fts = inject(FinancialTransactionsService);
  toolbarConfig!: ToolbarConfig;
  additionalGridFilters: GridFilter[] = [
    {
      field: DATA_FIELDS.TRANSACTION_TYPE,
      operator: FILTER_OPERATORS.EQUALS,
      value: TRANSACTION_TYPES.INCOME,
    },
  ];

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfigExcludeActivateInactivate();
  }

  fetchTitle = (rowData: any, col: GridColumn) => {
    if (col.field === DATA_FIELDS.LOCATION) {
      return rowData[DATA_FIELDS.LOCATION] + ' ~ ' + rowData[DATA_FIELDS.LOCATION_DESCRIPTION];
    }
    return rowData[col.field];
  };

  addRow = () => {
    this.dialog
      .open(AddCopyEditFinancialTransactionDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          transactionType: TRANSACTION_TYPES.INCOME,
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

  editRow = () => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.edit.noSelection'));
      return;
    }
    if (this.dataGrid?.selectedRows.length > 1) {
      this._ns.error(this._ms.get('common.edit.singleSelection'));
      return;
    }
    this.dialog
      .open(AddCopyEditFinancialTransactionDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          transactionType: TRANSACTION_TYPES.INCOME,
          mode: MODES.EDIT,
          selectedRow: this.dataGrid?.selectedRows[0],
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

  copyRow = () => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.copy.noSelection'));
      return;
    }
    if (this.dataGrid?.selectedRows.length > 1) {
      this._ns.error(this._ms.get('common.copy.singleSelection'));
      return;
    }
    this.dialog
      .open(AddCopyEditFinancialTransactionDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          transactionType: TRANSACTION_TYPES.INCOME,
          mode: MODES.COPY,
          selectedRow: this.dataGrid?.selectedRows[0],
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

  deleteRow = () => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: {
          title: this._ms.get('common.delete.title'),
          message: this._ms.get('financialTransaction.delete.confirmation'),
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid?.selectedRows.map((row) => row.id);
          firstValueFrom(this._fts.deleteFinancialTransactions(ids)).then((resp: ApiResponse) => {
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
