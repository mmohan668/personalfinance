import { AsyncPipe } from '@angular/common';
import { Component, computed, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';

import { SelectItem, SortEvent } from 'primeng/api';
import { Table, TableModule } from 'primeng/table';

import { BehaviorSubject } from 'rxjs';

import { ColumnFilterComponent } from './../column-filter/column-filter';
import { GridColumn, GridFilter, GridResult, SearchCriteria } from '../types/types';
import { GridService } from '../service/grid-service';
import { SORT_ICONS, SORT_ORDERS } from '../enums';
import { MatInputModule } from '@angular/material/input';
import * as FileSaver from 'file-saver';

@Component({
  selector: 'app-data-grid',
  standalone: true,

  imports: [
    TableModule,
    ColumnFilterComponent,
    AsyncPipe,

    MatSelectModule,
    MatFormFieldModule,
    MatCheckboxModule,
    FormsModule,
    MatMenuModule,
    MatIconModule,
    MatInputModule,
  ],

  styleUrl: './data-grid.scss',
  templateUrl: './data-grid.html',
})
export class DataGrid implements OnInit {
  @ViewChild('dt')
  dt!: Table;

  dataSourceSubject = new BehaviorSubject<any[]>([]);

  dataSource$ = this.dataSourceSubject.asObservable();

  filters: GridFilter[] = [];

  sortField = '';

  sortOrder: 1 | -1 = 1;

  totalRecords: number = 0;

  columns: GridColumn[] = [];

  visibleColumnsSubject = new BehaviorSubject<GridColumn[]>([]);

  visibleColumns$ = this.visibleColumnsSubject.asObservable();

  columnOptions: SelectItem[] = [];

  selectedRows: any[] = [];

  skip: number = 0;

  take: number = 5;

  constructor(private gridService: GridService) {}

  /* =========================================================
     INIT
     ========================================================= */

  ngOnInit(): void {
    this.gridService.loadGridColumns().subscribe((columns) => {
      this.columns = columns.map((column) => ({
        ...column,
        visible: column.visible !== false,
      }));

      this.visibleColumnsSubject.next(this.columns.filter((column) => column.visible !== false));

      this.columnOptions = this.columns.map((col) => ({
        label: col.header,
        value: col,
      }));

      this.initializeDefaultSort(this.columns);

      this.loadGridData();
    });
  }

  /* =========================================================
     DATA
     ========================================================= */

  loadGridData(): void {
    console.log('GET DATA');

    const request: SearchCriteria = {
      sort: {
        field: this.sortField,
        order: this.sortOrder === 1 ? SORT_ORDERS.ASCENDING : SORT_ORDERS.DESCENDING,
      },
      filterList: this.filters,
      skip: this.skip,
      take: this.take,
      loadAllData: false,
    };

    this.gridService.loadGridData(request).subscribe((data: GridResult) => {
      this.dataSourceSubject.next(data.recordDetails);
      this.totalRecords = data.totalRecords;
      /*
       * Remove selections that no longer exist
       * in the current data set.
       */
      const currentIds = new Set(data.recordDetails.map((row) => row.id));

      this.selectedRows = this.selectedRows.filter((row) => currentIds.has(row.id));
    });
  }

  /* =========================================================
     DEFAULT SORT
     ========================================================= */

  initializeDefaultSort(columns: GridColumn[]): void {
    const defaultSortColumn = columns.find(
      (column) => column.sortable !== false && column.defaultSortOrder !== undefined,
    );

    if (!defaultSortColumn) {
      return;
    }

    this.sortField = defaultSortColumn.field;

    this.sortOrder = defaultSortColumn.defaultSortOrder === SORT_ORDERS.DESCENDING ? -1 : 1;
  }

  /* =========================================================
     SORT
     ========================================================= */

  onSort(event: SortEvent): void {
    this.sortField = event.field ?? '';

    this.sortOrder = event.order === -1 ? -1 : 1;

    this.loadGridData();
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) {
      return '';
    }

    return this.sortOrder === 1 ? SORT_ICONS.ASCENDING : SORT_ICONS.DESCENDING;
  }

  onPage(event: any): void {
    const loadData = this.skip !== event.first;
    this.skip = event.first;
    this.take = event.rows;
    if (loadData) {
      this.loadGridData();
    }
  }

  /* =========================================================
     FILTER
     ========================================================= */

  changeOperator(gridFilter: GridFilter): void {
    this.filters.push(gridFilter);

    this.loadGridData();
  }

  applyFilter(gridFilter: GridFilter): void {
    this.filters.push(gridFilter);

    this.loadGridData();
  }

  /* =========================================================
     COLUMN VISIBILITY
     ========================================================= */

  toggleColumn(col: GridColumn, checked: boolean): void {
    const column = this.columns.find((column) => column.field === col.field);

    if (!column) {
      return;
    }

    column.visible = checked;

    this.visibleColumnsSubject.next(this.columns.filter((column) => column.visible !== false));
  }

  /* =========================================================
     ROW SELECTION
     ========================================================= */

  isRowSelected(row: any): boolean {
    return this.selectedRows.some((selected) => selected.id === row.id);
  }

  toggleRow(row: any, checked: boolean): void {
    if (checked) {
      if (!this.isRowSelected(row)) {
        this.selectedRows = [...this.selectedRows, row];
      }

      return;
    }

    this.selectedRows = this.selectedRows.filter((selected) => selected.id !== row.id);
  }

  /* =========================================================
     HEADER SELECTION STATE
     ========================================================= */

  allRowsSelected(): boolean {
    const rows = this.dataSourceSubject.value;

    if (rows.length === 0) {
      return false;
    }

    return rows.every((row) => this.isRowSelected(row));
  }

  someRowsSelected(): boolean {
    const rows = this.dataSourceSubject.value;

    if (rows.length === 0) {
      return false;
    }

    const selectedCount = rows.filter((row) => this.isRowSelected(row)).length;

    return selectedCount > 0 && selectedCount < rows.length;
  }

  /* =========================================================
     SELECT / UNSELECT ALL
     ========================================================= */

  toggleAllRows(checked: boolean): void {
    const rows = this.dataSourceSubject.value;

    if (checked) {
      /*
       * Add current page rows while
       * preserving existing selections.
       */
      const selectedMap = new Map(this.selectedRows.map((row) => [row.id, row]));

      for (const row of rows) {
        selectedMap.set(row.id, row);
      }

      this.selectedRows = Array.from(selectedMap.values());

      return;
    }

    /*
     * Remove only the rows from the
     * current data set.
     */
    const rowIds = new Set(rows.map((row) => row.id));

    this.selectedRows = this.selectedRows.filter((row) => !rowIds.has(row.id));
  }

  columnSearch = '';

  filteredColumns = computed(() => {
    const search = this.columnSearch.trim().toLowerCase();

    if (!search) {
      return this.columns;
    }

    return this.columns.filter((col) => col.header.toLowerCase().includes(search));
  });

  allColumnsSelected(): boolean {
    return this.columns.length > 0 && this.columns.every((col) => col.visible !== false);
  }

  someColumnsSelected(): boolean {
    const selectedCount = this.columns.filter((col) => col.visible !== false).length;

    return selectedCount > 0 && selectedCount < this.columns.length;
  }

  toggleAllColumns(checked: boolean): void {
    this.columns.forEach((col) => {
      col.visible = checked;
    });
    const selectedColumns = this.columns.filter((col) => col.visible === true);
    this.visibleColumnsSubject.next(selectedColumns);
  }

  getColumns(searchInput: string) {
    if (searchInput !== undefined && searchInput !== null && searchInput !== '') {
      const tempColumns = this.columns.filter((col) =>
        col.header.toLowerCase().includes(searchInput.toLowerCase()),
      );
      return tempColumns;
    }
    return this.columns;
  }

  exportExcel(selectionType: string) {
    import('xlsx').then((xlsx) => {
      const headers = this.visibleColumnsSubject.value.map((col) => col.header);
      let rows: any[] = [];
      if (selectionType === 'SELECTED') {
        if (this.selectedRows.length === 0) {
          console.log('No rows selected');
          return;
        }
        rows = this.selectedRows.map((row) =>
          this.visibleColumnsSubject.value.map((col) => row[col.field]),
        );
      } else {
        rows = this.dataSourceSubject.value.map((row) =>
          this.visibleColumnsSubject.value.map((col) => row[col.field]),
        );
      }
      const worksheet = xlsx.utils.aoa_to_sheet([headers, ...rows]);
      const workbook = { Sheets: { data: worksheet }, SheetNames: ['data'] };
      const excelBuffer: any = xlsx.write(workbook, { bookType: 'xlsx', type: 'array' });
      this.saveAsExcelFile(excelBuffer, 'products');
    });
  }

  saveAsExcelFile(buffer: any, fileName: string): void {
    const EXCEL_TYPE =
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
    const EXCEL_EXTENSION = '.xlsx';
    const data: Blob = new Blob([buffer], { type: EXCEL_TYPE });
    FileSaver.saveAs(data, fileName + '_export_' + new Date().getTime() + EXCEL_EXTENSION);
  }
}
