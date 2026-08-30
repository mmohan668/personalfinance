import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';

import { SelectItem, SortEvent, SortMeta } from 'primeng/api';
import { Table, TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';

import { BehaviorSubject } from 'rxjs';

import { ColumnFilterComponent } from './../column-filter/column-filter';
import { GridColumn, GridFilter, GridResult, GridSort, SearchCriteria } from '../types/types';

import { GridService } from '../service/grid-service';
import { SORT_ICONS, SORT_ORDERS } from '../enums';

import * as FileSaver from 'file-saver';
import { ToolBar } from '../tool-bar/tool-bar';

@Component({
  selector: 'app-data-grid',
  standalone: true,

  imports: [
    TableModule,
    ColumnFilterComponent,
    AsyncPipe,
    ToolBar,
    MatSelectModule,
    MatFormFieldModule,
    MatCheckboxModule,
    FormsModule,
    MatMenuModule,
    MatIconModule,
    MatInputModule,
    PaginatorModule,
    CurrencyPipe,
  ],

  styleUrl: './data-grid.scss',
  templateUrl: './data-grid.html',
})
export class DataGrid implements OnInit {
  @ViewChild('dt')
  dt!: Table;

  /* =========================================================
     DATA
     ========================================================= */

  dataSourceSubject = new BehaviorSubject<any[]>([]);

  dataSource$ = this.dataSourceSubject.asObservable();

  totalRecords = 0;

  skip = 0;

  take = 25;

  /* =========================================================
     FILTER
     ========================================================= */

  filters: GridFilter[] = [];

  /* =========================================================
     COLUMNS
     ========================================================= */

  columns: GridColumn[] = [];

  visibleColumnsSubject = new BehaviorSubject<GridColumn[]>([]);

  visibleColumns$ = this.visibleColumnsSubject.asObservable();

  columnOptions: SelectItem[] = [];

  columnSearch = '';

  /* =========================================================
     ROW SELECTION
     ========================================================= */

  selectedRows: any[] = [];

  /* =========================================================
     SORT
     =========================================================
     
     IMPORTANT:
     PrimeNG uses:
     
       order = 1  -> ascending
       order = -1 -> descending

     Keep PrimeNG's SortMeta[] internally.

     Only convert to GridSort[] when sending
     the request to the backend.
     ========================================================= */

  sortMeta: SortMeta[] = [];

  sortField = '';

  sortOrder: 1 | -1 = 1;

  @Input()
  calculateCellValue!: (rowData: any, col: GridColumn) => any;

  currencyCode: string = 'INR';

  @Input() groupBy: string | null = null;

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

      this.visibleColumnsSubject.next(this.columns.filter((column) => column.visible === true));

      this.columnOptions = this.columns.map((col) => ({
        label: col.header,
        value: col,
      }));

      /*
       * Build the initial/default sort state.
       *
       * This is important because the table is using
       * sortMode="multiple".
       */
      this.initializeDefaultSort(this.columns);

      /*
       * Load the initial data using the default sort.
       */
      this.loadGridData();
    });
  }

  /* =========================================================
     DATA
     ========================================================= */

  loadGridData(): void {
    console.log('GET DATA');

    /*
     * PrimeNG SortMeta[]
     *
     * Example:
     *
     * [
     *   { field: 'name', order: 1 },
     *   { field: 'price', order: -1 }
     * ]
     *
     * becomes:
     *
     * [
     *   { field: 'name', order: 'asc' },
     *   { field: 'price', order: 'desc' }
     * ]
     */
    const sorts: GridSort[] = this.sortMeta
      .filter(
        (sort): sort is SortMeta =>
          !!sort && typeof sort.field === 'string' && (sort.order === 1 || sort.order === -1),
      )
      .map((sort) => ({
        field: sort.field,
        order: sort.order === 1 ? 'asc' : 'desc',
      }));

    console.log('SORT META:', this.sortMeta);

    console.log('SORT LIST:', sorts);

    const request: SearchCriteria = {
      sortList: sorts,
      filterList: this.filters,
      skip: this.skip,
      take: this.take,
      loadAllData: false,
    };

    console.log('REQUEST:', request);

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
      /*
       * No default sort.
       */
      this.sortMeta = [];
      this.sortField = '';
      this.sortOrder = 1;

      console.log('DEFAULT SORT: []');

      return;
    }

    const order: 1 | -1 = defaultSortColumn.defaultSortOrder === SORT_ORDERS.DESCENDING ? -1 : 1;

    /*
     * Keep the default sort in PrimeNG's multi-sort state.
     *
     * This is the important difference from the previous code.
     */
    this.sortMeta = [
      {
        field: defaultSortColumn.field,
        order,
      },
    ];

    /*
     * These are kept for compatibility with the rest
     * of the component.
     */
    this.sortField = defaultSortColumn.field;
    this.sortOrder = order;

    console.log('DEFAULT SORT:', this.sortMeta);
  }

  /* =========================================================
     SORT
     ========================================================= */

  onSort(event: SortEvent): void {
    console.log('SORT EVENT:', event);

    /*
     * IMPORTANT:
     *
     * Your installed PrimeNG version emits:
     *
     *   event.multisortmeta
     *
     * rather than:
     *
     *   event.multiSortMeta
     *
     * This is why the previous code was always getting [].
     *
     * Use both here so this code is safe with either event shape.
     */
    const eventData = event as SortEvent & {
      multisortmeta?: SortMeta[];
    };

    const multiSortMeta = eventData.multisortmeta ?? eventData.multiSortMeta ?? [];

    /*
     * MULTIPLE SORT
     *
     * Example after Ctrl-clicking Name and Category:
     *
     * [
     *   { field: 'name', order: 1 },
     *   { field: 'category', order: 1 }
     * ]
     */
    if (multiSortMeta.length > 0) {
      this.sortMeta = multiSortMeta.map((sort) => ({
        field: sort.field,
        order: sort.order === -1 ? -1 : 1,
      }));

      /*
       * Keep these values synchronized with the first
       * active sort item.
       */
      this.sortField = this.sortMeta[0]?.field ?? '';

      this.sortOrder = this.sortMeta[0]?.order === -1 ? -1 : 1;
    } else {
      /*
       * If PrimeNG sends an empty multi-sort list,
       * clear the sort state.
       */
      this.sortMeta = [];

      this.sortField = '';

      this.sortOrder = 1;
    }

    console.log('UPDATED PRIMARY SORT META:', this.sortMeta);

    /*
     * Sorting should start from the first page.
     *
     * Do NOT use:
     *
     * this.dt.first = 0;
     *
     * in newer PrimeNG versions because first can be
     * represented as an Angular signal input.
     *
     * We only need to reset our backend pagination state.
     */
    this.skip = 0;

    /*
     * Reload using the new sort state.
     */
    this.loadGridData();
  }

  /* =========================================================
     SORT ICON
     ========================================================= */

  getSortIcon(field: string): string {
    const sort = this.sortMeta.find((item) => item.field === field);

    if (!sort) {
      return '';
    }

    return sort.order === 1 ? SORT_ICONS.ASCENDING : SORT_ICONS.DESCENDING;
  }

  /* =========================================================
     PAGE
     ========================================================= */

  onPage(event: any): void {
    const loadData = this.skip !== event.first || this.take !== event.rows;

    this.skip = event.first ?? 0;

    this.take = event.rows ?? 5;

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

  toggleColumn = (col: GridColumn, checked: boolean): void => {
    const column = this.columns.find((column) => column.field === col.field);

    if (!column) {
      return;
    }

    column.visible = checked;

    this.visibleColumnsSubject.next(this.columns.filter((column) => column.visible === true));
  };

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

  /* =========================================================
     COLUMN SEARCH
     ========================================================= */

  getColumns = (searchInput: string): GridColumn[] => {
    if (searchInput !== undefined && searchInput !== null && searchInput !== '') {
      return this.columns.filter((col) =>
        col.header.toLowerCase().includes(searchInput.toLowerCase()),
      );
    }

    return this.columns;
  };

  /* =========================================================
     COLUMN SELECTION
     ========================================================= */

  allColumnsSelected = (): boolean => {
    return this.columns.length > 0 && this.columns.every((col) => col.visible !== false);
  };

  someColumnsSelected = (): boolean => {
    const selectedCount = this.columns.filter((col) => col.visible !== false).length;

    return selectedCount > 0 && selectedCount < this.columns.length;
  };

  toggleAllColumns = (checked: boolean): void => {
    this.columns.forEach((col) => {
      col.visible = checked;
    });

    this.visibleColumnsSubject.next(this.columns.filter((col) => col.visible === true));
  };

  /* =========================================================
     EXPORT
     ========================================================= */

  exportExcel = (selectionType: string): void => {
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

      const workbook = {
        Sheets: {
          data: worksheet,
        },
        SheetNames: ['data'],
      };

      const excelBuffer: any = xlsx.write(workbook, {
        bookType: 'xlsx',
        type: 'array',
      });

      this.saveAsExcelFile(excelBuffer, 'products');
    });
  };

  /* =========================================================
     SAVE EXCEL
     ========================================================= */

  saveAsExcelFile(buffer: any, fileName: string): void {
    const EXCEL_TYPE =
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';

    const EXCEL_EXTENSION = '.xlsx';

    const data: Blob = new Blob([buffer], {
      type: EXCEL_TYPE,
    });

    FileSaver.saveAs(data, fileName + '_export_' + new Date().getTime() + EXCEL_EXTENSION);
  }

  onColReorder(event: any) {
    const moved = this.columns.splice(event.dragIndex, 1)[0];
    this.columns.splice(event.dropIndex, 0, moved);
    this.visibleColumnsSubject.next(this.columns.filter((col) => col.visible === true));
  }

  getGroupColumnHeader() {
    return this.columns.filter((col) => col.field === this.groupBy)[0].header;
  }
}
