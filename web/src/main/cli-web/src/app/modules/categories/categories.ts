import { ChangeDetectionStrategy, Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import {
  DATA_FIELDS,
  FILTER_OPERATORS,
  GRID_EXPORT_FILE_NAMES,
  GRID_NAMES,
  MODES,
} from '../shared/enums';
import { ApiResponse, GridColumn, GridFilter, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { AddEditCategoryDialog } from './add-edit-category-dialog/add-edit-category-dialog';
import { firstValueFrom } from 'rxjs';
import { CategoryManagementService } from '../shared/service/category-management-service';
import { NotificationService } from '../shared/service/notification-service';
import { MessageService } from '../shared/service/message-service';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { ChoiceDialog } from '../shared/choice-dialog/choice-dialog';

@Component({
  imports: [DataGrid],
  selector: 'app-categories',
  styleUrl: './categories.scss',
  templateUrl: './categories.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Categories {
  @ViewChild('dataGrid') private dataGrid!: DataGrid;

  public readonly _cs = inject(CommonService);
  protected readonly dialog = inject(MatDialog);
  private readonly _cms = inject(CategoryManagementService);
  protected readonly _ns = inject(NotificationService);
  protected readonly _ms = inject(MessageService);

  protected readonly gridName = GRID_NAMES.CATEGORIES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.CATEGORIES_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly toolbaConfig!: ToolbarConfig;
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 1 },
  ];

  constructor() {
    this.toolbaConfig = this._cs.toolbarConfig(true);
    this.toolbaConfig.copyRow = false;
  }

  calculateCellValue = (rowData: any, col: GridColumn): any => {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  };

  addRow = (): void => {
    this.dialog
      .open(AddEditCategoryDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.ADD,
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((refresh: boolean) => {
        if (refresh) {
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
      .open(AddEditCategoryDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.EDIT,
          selectedRow: this.dataGrid.selectedRows[0],
        },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((refresh: boolean) => {
        if (refresh) {
          this.dataGrid?.refreshGrid();
        }
      });
  };

  deleteRow = (): void => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    const title = this._ms.get('common.delete.title');
    const message = this._ms.get('category.delete.confirmation');
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
          const ids = this.dataGrid?.selectedRows.map((row) => row.id);
          firstValueFrom(this._cms.deleteCategories(ids)).then((resp: ApiResponse) => {
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

  activate = (): void => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.activate.noSelection'));
      return;
    }
    if (this.dataGrid?.selectedRows.every((row) => row.active === true)) {
      this._ns.error(this._ms.get('category.activate.alreadyActive'));
      return;
    }
    const title = this._ms.get('category.activate.title');
    const message = this.dataGrid?.selectedRows.some((row) => row.active === true)
      ? this._ms.get('category.activate.mixedSelection')
      : this._ms.get('category.activate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: { title: title, message: message },
        disableClose: true,
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          this.dialog
            .open(ChoiceDialog, {
              width: 'auto',
              data: {
                title: 'Confirmation',
                message: 'Do you want to activate subcategories as well?',
              },
            })
            .afterClosed()
            .subscribe((value: string) => {
              if (value) {
                const ids = this.dataGrid?.selectedRows
                  .filter((row) => row.active !== true)
                  .map((row) => row.id);
                firstValueFrom(this._cms.activateCategories(ids, value === 'Yes')).then((resp) => {
                  if (resp.success) {
                    this._ns.success(resp.message);
                    this.dataGrid?.refreshGrid();
                  } else {
                    this._ns.error(resp.message);
                  }
                });
              }
            });
        }
      });
  };

  inactivate = (): void => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.inactivate.noSelection'));
      return;
    }
    if (this.dataGrid?.selectedRows.every((row) => row.active === false)) {
      this._ns.error(this._ms.get('category.inactivate.alreadyInactive'));
      return;
    }
    const title = this._ms.get('category.inactivate.title');
    const message = this.dataGrid?.selectedRows.some((row) => row.active === false)
      ? this._ms.get('category.inactivate.mixedSelection')
      : this._ms.get('category.inactivate.confirmation');
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
            .filter((row) => row.active === true)
            .map((row) => row.id);
          firstValueFrom(this._cms.inactivateCategories(ids)).then((resp: ApiResponse) => {
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
