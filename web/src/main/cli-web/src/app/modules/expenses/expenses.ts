import { Component, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';

@Component({
  imports: [DataGrid],
  selector: 'app-expenses',
  styleUrl: './expenses.scss',
  templateUrl: './expenses.html',
})
export class Expenses {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridName = GRID_NAMES.EXPENSES_GRID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.EXPENSES_GRID_EFN;
}
