import { Component } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { GRID_NAMES } from '../shared/enums';
import { GridColumn } from '../shared/types/types';

@Component({
  imports: [DataGrid],
  standalone: true,
  selector: 'app-dashboard',
  styleUrl: './dashboard.scss',
  templateUrl: './dashboard.html',
})
export class Dashboard {
  protected readonly gridName = GRID_NAMES.PRODUCT_GRID;

  booleanOptions = [
    { label: 'All', value: null },
    { label: 'Active', value: true },
    { label: 'Inactive', value: false },
  ];

  calculateCellValue(rowData: any, col: GridColumn) {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    if (col.field === 'rating') {
      const rating = Math.round(rowData[col.field]);
      let stars = '';
      for (let i = 1; i <= rowData[col.field]; i++) {
        stars += `*`;
      }
      return stars;
    }
    return rowData[col.field];
  }
}
