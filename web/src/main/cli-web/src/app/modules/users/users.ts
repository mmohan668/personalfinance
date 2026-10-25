import { ChangeDetectionStrategy, Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '../shared/service/notification-service';
import { DATA_FIELDS, FILTER_OPERATORS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { GridColumn, GridFilter, ToolbarConfig } from '../shared/types/types';
import { MessageService } from '../shared/service/message-service';

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

  protected readonly gridName = GRID_NAMES.USERS_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.USERS_GRID_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly toolbaConfig!: ToolbarConfig;
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.ADMIN_USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 2 },
  ];

  constructor() {
    this.toolbaConfig = this._cs.toolbarConfig();
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
}
