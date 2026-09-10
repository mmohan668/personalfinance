import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, FILTER_OPERATORS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { GridColumn, GridFilter } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';

@Component({
  imports: [DataGrid],
  selector: 'app-categories',
  styleUrl: './categories.scss',
  templateUrl: './categories.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Categories {
  protected readonly gridName = GRID_NAMES.CATEGORIES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.CATEGORIES_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 1 },
  ];
  public _cs = inject(CommonService);

  constructor() {}

  calculateCellValue = (rowData: any, col: GridColumn) => {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  };
}
