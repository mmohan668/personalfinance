import { Component, inject, signal, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {
  DATA_FIELDS,
  ENTITIES,
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
import {
  ApiResponse,
  GridColumn,
  GridFilter,
  SuggestionRequest,
  ToolbarConfig,
} from '../shared/types/types';
import { AddCopyEditFinancialTransactionDialog } from '../shared/add-copy-edit-financialtransaction-dialog/add-copy-edit-financialtransaction-dialog';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { debounceTime, firstValueFrom } from 'rxjs';
import { DataGrid } from '../shared/data-grid/data-grid';
import { FormControl, FormGroup } from '@angular/forms';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { AppConfigService } from '../shared/service/app-config-service';
import { SuggestionService } from '../shared/service/suggestion-service';

@Component({
  imports: [DataGrid, CommonImportsModule],
  selector: 'app-transfers',
  styleUrl: './transfers.scss',
  templateUrl: './transfers.html',
})
export class Transfers {
  @ViewChild('dataGrid') private dataGrid!: DataGrid;

  private readonly dialog = inject(MatDialog);
  public readonly _cs = inject(CommonService);
  private readonly _ns = inject(NotificationService);
  private readonly _ms = inject(MessageService);
  private readonly _fts = inject(FinancialTransactionsService);
  private readonly _acs = inject(AppConfigService);
  private readonly _ss = inject(SuggestionService);

  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridName = GRID_NAMES.TRANSFERS_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.TRANSFERS_GRID_EFN;
  protected readonly toolbarConfig!: ToolbarConfig;
  protected readonly additionalGridFilters: GridFilter[] = [
    {
      field: DATA_FIELDS.TRANSACTION_TYPE,
      operator: FILTER_OPERATORS.EQUALS,
      value: TRANSACTION_TYPES.TRANSFER,
    },
  ];
  protected searchForm!: FormGroup;
  protected readonly fieldCount = 2;
  protected readonly maxDate = new Date();

  locations = signal<string[]>([]);
  categoryNames = signal<string[]>([]);
  subcategoryNames = signal<string[]>([]);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfigExcludeActivateInactivate(
      'Financial Transaction',
      'Transfer',
    );
    this.toolbarConfig.refresh = true;
    this.createSearchForm();
    this.fetchSuggestions();
  }

  createSearchForm() {
    this.searchForm = new FormGroup({
      categoryName: new FormControl(''),
      subcategoryName: new FormControl(''),
      location: new FormControl(''),
      transactionFromDate: new FormControl(''),
      transactionThruDate: new FormControl(''),
    });
  }

  fetchSuggestions() {
    this.searchForm.controls['categoryName'].valueChanges
      .pipe(debounceTime(this._acs.configValue.debounceTime))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.USER_CATEGORY,
            field: DATA_FIELDS.CATEGORY_NAME,
            value: val,
            dependentField: DATA_FIELDS.REFERENCE_VALUE_REFFERENCE_CODE,
            dependentValue: TRANSACTION_TYPES.TRANSFER,
          };
          firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.categoryNames.set(resp);
          });
        }
      });

    this.searchForm.controls['subcategoryName'].valueChanges
      .pipe(debounceTime(this._acs.configValue.debounceTime))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.USER_SUBCATEGORY,
            field: DATA_FIELDS.SUBCATEGORY_NAME,
            value: val,
            dependentField: DATA_FIELDS.USER_CATEGORY_REFERENCE_VALUE_REFFERENCE_CODE,
            dependentValue: TRANSACTION_TYPES.TRANSFER,
          };
          firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.subcategoryNames.set(resp);
          });
        }
      });

    this.searchForm.controls['location'].valueChanges
      .pipe(debounceTime(this._acs.configValue.debounceTime))
      .subscribe((val) => {
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
          transactionType: TRANSACTION_TYPES.TRANSFER,
          mode: MODES.ADD,
        },
      })
      .afterClosed()
      .subscribe((value: any) => {
        if (value) {
          if (typeof value === 'string') {
            this._ns.success(value);
          }
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
          transactionType: TRANSACTION_TYPES.TRANSFER,
          mode: MODES.EDIT,
          selectedRow: this.dataGrid?.selectedRows[0],
        },
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
          transactionType: TRANSACTION_TYPES.TRANSFER,
          mode: MODES.COPY,
          selectedRow: this.dataGrid?.selectedRows[0],
        },
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
    this.searchForm.reset();
    this.dataGrid.resetAllFilters();
  }
}
