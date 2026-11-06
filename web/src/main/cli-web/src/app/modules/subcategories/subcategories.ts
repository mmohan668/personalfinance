import { Component, inject, signal, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import {
  DATA_FIELDS,
  ENTITIES,
  FILTER_OPERATORS,
  GRID_EXPORT_FILE_NAMES,
  GRID_NAMES,
  MODES,
  STATUS,
} from '../shared/enums';
import {
  ApiResponse,
  GridColumn,
  GridFilter,
  SuggestionRequest,
  ToolbarConfig,
} from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { AddEditSubCategoryDialog } from './add-edit-sub-category-dialog/add-edit-sub-category-dialog';
import { NotificationService } from '../shared/service/notification-service';
import { MessageService } from '../shared/service/message-service';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { CategoryManagementService } from '../shared/service/category-management-service';
import { debounceTime, firstValueFrom } from 'rxjs';
import { FormControl, FormGroup } from '@angular/forms';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { SuggestionService } from '../shared/service/suggestion-service';
import { AppConfigService } from '../shared/service/app-config-service';

@Component({
  imports: [DataGrid, CommonImportsModule],
  selector: 'app-subcategories',
  styleUrl: './subcategories.scss',
  templateUrl: './subcategories.html',
})
export class Subcategories {
  @ViewChild('dataGrid') private dataGrid!: DataGrid;

  public readonly _cs = inject(CommonService);
  private readonly dialog = inject(MatDialog);
  private readonly _ns = inject(NotificationService);
  private readonly _ms = inject(MessageService);
  private readonly _cms = inject(CategoryManagementService);
  private readonly _ss = inject(SuggestionService);
  private readonly _acs = inject(AppConfigService);

  protected readonly gridName = GRID_NAMES.SUBCATEGORIES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.SUBCATEGORIES_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly toolbarConfig: ToolbarConfig;

  protected searchForm!: FormGroup;
  fieldCount = 4;

  transactionTypes = signal<string[]>([]);
  categoryNames = signal<string[]>([]);
  subcategoryNames = signal<string[]>([]);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig(true);
    this.createSearchForm();
    this.fetchSuggestions();
  }

  createSearchForm() {
    this.searchForm = new FormGroup({
      transactionType: new FormControl(''),
      categoryName: new FormControl(''),
      subcategoryName: new FormControl(''),
      isActive: new FormControl(''),
    });
  }

  fetchSuggestions() {
    this.searchForm.controls['transactionType'].valueChanges
      .pipe(debounceTime(this._acs.configValue.debounceTime))
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
      .pipe(debounceTime(this._acs.configValue.debounceTime))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.USER_CATEGORY,
            field: DATA_FIELDS.CATEGORY_NAME,
            value: val,
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
          };
          firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.subcategoryNames.set(resp);
          });
        }
      });
  }

  calculateCellValue = (rowData: any, col: GridColumn) => {
    if (col.field === DATA_FIELDS.IS_ACTIVE) {
      return rowData[col.field] ? STATUS.ACTIVE : STATUS.INACTIVE;
    }
    return rowData[col.field];
  };

  addRow = () => {
    this.dialog
      .open(AddEditSubCategoryDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.ADD,
        },
      })
      .afterClosed()
      .subscribe((value: any) => {
        if (value) {
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
      .open(AddEditSubCategoryDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.EDIT,
          selectedRow: this.dataGrid?.selectedRows[0],
        },
      })
      .afterClosed()
      .subscribe((value: any) => {
        if (value) {
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
      .open(AddEditSubCategoryDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.COPY,
          selectedRow: this.dataGrid?.selectedRows[0],
        },
      })
      .afterClosed()
      .subscribe((value: any) => {
        if (value) {
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
          title: this._ms.get('subcategory.delete.title'),
          message: this._ms.get('subcategory.delete.confirmation'),
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid?.selectedRows.map((row) => row.id);
          firstValueFrom(this._cms.deleteSubcategories(ids)).then((resp: ApiResponse) => {
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
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.activate.noSelection'));
      return;
    }
    if (this.dataGrid?.selectedRows.every((row) => row.isActive === true)) {
      this._ns.error(this._ms.get('subcategory.activate.alreadyActive'));
      return;
    }
    const title = this._ms.get('subcategory.activate.title');
    const message = this.dataGrid?.selectedRows.some((row) => row.isActive === true)
      ? this._ms.get('subcategory.activate.mixedSelection')
      : this._ms.get('subcategory.activate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: { title: title, message: message },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid?.selectedRows
            .filter((row) => row.isActive !== true)
            .map((row) => row.id);
          firstValueFrom(this._cms.activateSubcategories(ids)).then((resp) => {
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
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.inactivate.noSelection'));
      return;
    }
    if (this.dataGrid?.selectedRows.every((row) => row.isActive === false)) {
      this._ns.error(this._ms.get('subcategory.inactivate.alreadyInactive'));
      return;
    }
    const title = this._ms.get('subcategory.inactivate.title');
    const message = this.dataGrid?.selectedRows.some((row) => row.isActive === false)
      ? this._ms.get('subcategory.inactivate.mixedSelection')
      : this._ms.get('subcategory.inactivate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: { title: title, message: message },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid?.selectedRows
            .filter((row) => row.isActive === true)
            .map((row) => row.id);
          firstValueFrom(this._cms.inactivateSubcategories(ids)).then((resp: ApiResponse) => {
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
    this.searchForm.reset({
      isActive: '',
    });
    this.dataGrid.resetAllFilters();
  }
}
