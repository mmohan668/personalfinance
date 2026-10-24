import { Component, inject, signal, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import {
  DATA_FIELDS,
  ENTITIES,
  FILTER_OPERATORS,
  GRID_EXPORT_FILE_NAMES,
  GRID_NAMES,
  MODES,
} from '../shared/enums';
import { CommonService } from '../shared/service/common-service';
import {
  ApiResponse,
  GridColumn,
  GridFilter,
  SuggestionRequest,
  ToolbarConfig,
} from '../shared/types/types';
import { MatDialog } from '@angular/material/dialog';
import { AddCopyEditFinancialTransactionDialog } from '../shared/add-copy-edit-financialtransaction-dialog/add-copy-edit-financialtransaction-dialog';
import { NotificationService } from '../shared/service/notification-service';
import { MessageService } from '../shared/service/message-service';
import { FinancialTransactionsService } from '../shared/service/financial-transactions-service';
import { debounceTime, firstValueFrom } from 'rxjs';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { FormControl, FormGroup } from '@angular/forms';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { SuggestionService } from '../shared/service/suggestion-service';

@Component({
  imports: [DataGrid, CommonImportsModule],
  selector: 'app-all-transactions',
  styleUrl: './all-transactions.scss',
  templateUrl: './all-transactions.html',
})
export class AllTransactions {
  @ViewChild('dataGrid') private dataGrid!: DataGrid;

  private readonly dialog = inject(MatDialog);
  public readonly _cs = inject(CommonService);
  private readonly _ns = inject(NotificationService);
  private readonly _ms = inject(MessageService);
  private readonly _fts = inject(FinancialTransactionsService);
  private readonly _ss = inject(SuggestionService);

  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridName = GRID_NAMES.ALL_TRANSACTIONS_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.ALL_TRANSACTIONS_GRID_EFN;
  protected readonly toolbarConfig!: ToolbarConfig;
  protected searchForm!: FormGroup;
  protected readonly fieldCount = 3;
  protected readonly maxDate = new Date();

  transactionTypes = signal<string[]>([]);
  locations = signal<string[]>([]);
  categoryNames = signal<string[]>([]);
  subcategoryNames = signal<string[]>([]);
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.ADMIN_USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 2 },
  ];

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfigExcludeActivateInactivate();
    this.toolbarConfig.refresh = true;
    this.createSearchForm();
    this.fetchSuggestions();
  }

  createSearchForm() {
    this.searchForm = new FormGroup({
      transactionType: new FormControl(''),
      categoryName: new FormControl(''),
      subcategoryName: new FormControl(''),
      location: new FormControl(''),
      transactionFromDate: new FormControl(''),
      transactionThruDate: new FormControl(''),
    });
  }

  fetchSuggestions() {
    this.searchForm.controls['transactionType'].valueChanges
      .pipe(debounceTime(300))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.REFERENCE_VALUE,
            field: DATA_FIELDS.REFERENCE_CODE,
            value: val,
            dependentField: DATA_FIELDS.REFERENCE_OBJECT_REF_OBJ_NAME,
            dependentValue: 'TRANSACTION_TYPE',
          };
          firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.transactionTypes.set(resp);
          });
        }
      });

    this.searchForm.controls['categoryName'].valueChanges
      .pipe(debounceTime(300))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.USER_CATEGORY,
            field: DATA_FIELDS.CATEGORY_NAME,
            value: val,
            dependentField: DATA_FIELDS.USER_ID,
            dependentValue: '2',
          };
          firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.categoryNames.set(resp);
          });
        }
      });

    this.searchForm.controls['subcategoryName'].valueChanges
      .pipe(debounceTime(300))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.USER_SUBCATEGORY,
            field: DATA_FIELDS.SUBCATEGORY_NAME,
            value: val,
            dependentField: DATA_FIELDS.USER_CATEGORY_USER_ID,
            dependentValue: '2',
          };
          firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.subcategoryNames.set(resp);
          });
        }
      });

    this.searchForm.controls['location'].valueChanges.pipe(debounceTime(300)).subscribe((val) => {
      if (this._cs.isNotNull(val)) {
        const suggestionRequest: SuggestionRequest = {
          entity: ENTITIES.REFERENCE_VALUE,
          field: DATA_FIELDS.REFERENCE_CODE,
          value: val,
          dependentField: DATA_FIELDS.REFERENCE_OBJECT_REF_OBJ_NAME,
          dependentValue: 'LOCATION',
        };
        firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
          this.locations.set(resp);
        });
      }
    });
  }

  fetchTitle = (rowData: any, col: GridColumn): any => {
    if (col.field === DATA_FIELDS.LOCATION) {
      return rowData[DATA_FIELDS.LOCATION] + ' ~ ' + rowData[DATA_FIELDS.LOCATION_DESCRIPTION];
    }
    return rowData[col.field];
  };

  addRow = (): void => {
    this.dialog
      .open(AddCopyEditFinancialTransactionDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.ADD,
          allTransactions: true,
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

  editRow = (): void => {
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
          mode: MODES.EDIT,
          selectedRow: this.dataGrid?.selectedRows[0],
          allTransactions: true,
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

  copyRow = (): void => {
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
          mode: MODES.COPY,
          selectedRow: this.dataGrid?.selectedRows[0],
          allTransactions: true,
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

  deleteRow = (): void => {
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

  onSearch() {
    this.dataGrid.clearFilters();
    this.dataGrid.refreshGrid(this._cs.prepareSearchCriteria(this.searchForm.value));
  }

  resetSearchForm() {
    this.resetSuggestions();
    this.searchForm.reset();
    this.dataGrid.resetAllFilters();
  }

  resetSuggestions() {
    this.transactionTypes.set([]);
    this.categoryNames.set([]);
    this.subcategoryNames.set([]);
    this.locations.set([]);
  }
}
