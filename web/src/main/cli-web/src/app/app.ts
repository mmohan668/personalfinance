import { Component, signal } from '@angular/core';
import { DataGrid } from './modules/shared/data-grid/data-grid';
import { GridColumn } from './modules/shared/types/types';
import { GRID_NAMES } from './modules/shared/enums';

@Component({
  imports: [DataGrid],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('cli-web');
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
