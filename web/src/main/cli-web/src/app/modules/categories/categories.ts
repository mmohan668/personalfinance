import { Component } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, FILTER_OPERATORS, GRID_NAMES } from '../shared/enums';
import { GridColumn, GridFilter } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';

@Component({
  imports: [DataGrid],
  selector: 'app-categories',
  styleUrl: './categories.scss',
  templateUrl: './categories.html',
})
export class Categories {
  protected readonly gridName = GRID_NAMES.CATEGORIES_GRID;
  protected readonly additionalFilters: GridFilter[] = [
    { field: DATA_FIELDS.USER_ID, operator: FILTER_OPERATORS.EQUALS, value: 1 },
  ];

  constructor(public commonService: CommonService) {}

  calculateCellValue = (rowData: any, col: GridColumn) => {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  };
}
