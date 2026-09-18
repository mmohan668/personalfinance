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

@Component({
  imports: [DataGrid],
  selector: 'app-categories',
  styleUrl: './categories.scss',
  templateUrl: './categories.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Categories {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  protected readonly gridName = GRID_NAMES.CATEGORIES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.CATEGORIES_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly _ms = inject(MessageService);
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 1 },
  ];
  public _cs = inject(CommonService);
  protected toolbaConfig!: ToolbarConfig;
  private dialog = inject(MatDialog);
  private _cms = inject(CategoryManagementService);
  protected _ns = inject(NotificationService);

  constructor() {
    this.toolbaConfig = this._cs.toolbarConfig;
    this.toolbaConfig.addRow = true;
    this.toolbaConfig.editRow = true;
    this.toolbaConfig.deleteRow = true;
  }

  calculateCellValue = (rowData: any, col: GridColumn) => {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  };

  addRow = () => {
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

  deleteRow = () => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    const title = this._ms.get('common.delete.title');
    const message = this._ms.get('category.delete.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: '60vw',
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
          firstValueFrom(this._cms.deleteCategories(ids))
            .then((resp: ApiResponse) => {
              if (resp.success) {
                this._ns.success(resp.message);
                this.dataGrid?.refreshGrid();
              } else {
                this._ns.error(resp.message);
              }
            })
            .catch((error) => {
              console.log('Error while deleteCategories: ', error);
            });
        }
      });
  };
}
