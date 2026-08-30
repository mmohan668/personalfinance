import { Component, signal } from '@angular/core';
import { DataGrid } from './modules/shared/data-grid/data-grid';
import { GridColumn } from './modules/shared/types/types';

@Component({
  imports: [DataGrid],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('cli-web');

  calculateCellValue(rowData: any, col: GridColumn) {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  }
}
