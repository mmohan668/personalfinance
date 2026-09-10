import { Component, inject } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, FILTER_OPERATORS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { GridColumn, GridFilter } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';

@Component({
  imports: [DataGrid],
  selector: 'app-subcategories',
  styleUrl: './subcategories.scss',
  templateUrl: './subcategories.html',
})
export class Subcategories {
  protected readonly gridName = GRID_NAMES.SUBCATEGORIES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.SUBCATEGORIES_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 1 },
  ];
  public _cs = inject(CommonService);

  constructor() {}

  calculateCellValue = (rowData: any, col: GridColumn) => {
    if (col.field === 'isActive') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  };
}
