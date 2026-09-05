import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
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
import { Paginator, PaginatorModule } from 'primeng/paginator';

import { BehaviorSubject, firstValueFrom } from 'rxjs';

import { ColumnFilterComponent } from './../column-filter/column-filter';
import { GridColumn, GridFilter, GridResult, GridSort, SearchCriteria } from '../types/types';

import { GridService } from '../service/grid-service';
import { SORT_ICONS, SORT_ORDERS } from '../enums';

import * as FileSaver from 'file-saver';
import { ToolBar } from '../tool-bar/tool-bar';
import { CommonService } from '../service/common-service';

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
    DatePipe,
  ],
  providers: [CurrencyPipe, DatePipe],

  styleUrl: './data-grid.scss',
  templateUrl: './data-grid.html',
})
export class DataGrid implements OnInit {
  @ViewChild('dt') dt!: Table;
  @ViewChild('paginator') paginator!: Paginator;

  /* =========================================================
      TOOLBAR ACTIONS
      ========================================================= */

  @Input()
  addRow!: () => void;

  @Input()
  copyRow!: () => void;

  @Input()
  editRow!: () => void;

  @Input()
  deleteRow!: () => void;

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

  dateFormat: string = 'dd/MM/yyyy';

  /* =========================================================
     CONSTRUCTOR
     ========================================================= */

  constructor(
    private gridService: GridService,
    public commonService: CommonService,
    private currencyPipe: CurrencyPipe,
  ) {}

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

    const request: SearchCriteria = {
      sortList: sorts,
      filterList: this.filters,
      skip: this.skip,
      take: this.take,
      loadAllData: false,
    };

    this.gridService.loadGridData(request).subscribe({
      next: (result) => {
        this.dataSourceSubject.next(result.recordDetails);
        this.totalRecords = result.totalRecords;
      },
      error: (error) => {
        console.error('Error loading grid data:', error);
        this.dataSourceSubject.next([]);
        this.totalRecords = 0;
      },
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
    this.resetGrid();
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
    this.filters = this.filters.filter((filter) => filter.field !== gridFilter.field);
    if (
      gridFilter.operator === 'isNull' ||
      gridFilter.operator === 'isNotNull' ||
      this.commonService.isNotNull(gridFilter.value)
    ) {
      this.filters.push(gridFilter);
    }
    this.loadGridData();
  }

  applyFilter(gridFilter: GridFilter): void {
    this.filters = this.filters.filter((filter) => filter.field !== gridFilter.field);
    if (
      gridFilter.operator === 'isNull' ||
      gridFilter.operator === 'isNotNull' ||
      this.commonService.isNotNull(gridFilter.value)
    ) {
      this.filters.push(gridFilter);
    }
    this.resetGrid();
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

  onRowClick(row: any): void {
    this.toggleRow(row, !this.isRowSelected(row));
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

  exportExcel = async (selectionType: string): Promise<void> => {
    const module = await import('exceljs');
    const ExcelJS = module.default ?? module;

    const visibleColumns = this.visibleColumnsSubject.value;

    // =====================================================
    // SOURCE DATA
    // =====================================================

    let sourceRows: any[] = [];

    if (selectionType === 'SELECTED') {
      if (this.selectedRows.length === 0) {
        console.log('No rows selected');
        return;
      }

      sourceRows = this.selectedRows;
    } else {
      const sorts: GridSort[] = this.sortMeta
        .filter(
          (sort): sort is SortMeta =>
            !!sort && typeof sort.field === 'string' && (sort.order === 1 || sort.order === -1),
        )
        .map((sort) => ({
          field: sort.field,
          order: sort.order === 1 ? 'asc' : 'desc',
        }));

      const request: SearchCriteria = {
        sortList: sorts,
        filterList: this.filters,
        skip: this.skip,
        take: this.take,
        loadAllData: true,
      };

      console.log('REQUEST:', request);

      sourceRows = (await firstValueFrom(this.gridService.loadGridData(request))).recordDetails;
    }

    // =====================================================
    // CREATE WORKBOOK
    // =====================================================

    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('data');

    // Get currency symbol once.
    // Example:
    // USD -> $
    // INR -> ₹
    // EUR -> €
    // GBP -> £
    const currencySymbol = this.getCurrencySymbol();

    // =====================================================
    // MAIN HEADER
    // =====================================================

    const headerRow = worksheet.addRow(visibleColumns.map((col) => col.header));

    headerRow.eachCell((cell) => {
      cell.font = {
        bold: true,
        size: 13,
      };

      cell.alignment = {
        vertical: 'middle',
      };
    });

    // =====================================================
    // GROUP / DATA ROWS
    // =====================================================

    let previousGroup: any = Symbol('initial');

    for (const row of sourceRows) {
      const currentGroup = this.groupBy ? row[this.groupBy] : undefined;

      // ===================================================
      // GROUP HEADER
      // ===================================================

      if (this.groupBy && currentGroup !== previousGroup) {
        const groupHeaderRow = worksheet.addRow([
          `${this.getGroupColumnHeader()}: ${currentGroup}`,
        ]);

        // Merge group header across all visible columns
        if (visibleColumns.length > 1) {
          worksheet.mergeCells(
            groupHeaderRow.number,
            1,
            groupHeaderRow.number,
            visibleColumns.length,
          );
        }

        // Group header styling
        const groupCell = groupHeaderRow.getCell(1);

        groupCell.font = {
          bold: true,
          size: 13,
        };

        groupCell.alignment = {
          vertical: 'middle',
        };

        previousGroup = currentGroup;
      }

      // ===================================================
      // DATA ROW
      // ===================================================

      const dataRow = worksheet.addRow(
        visibleColumns.map((col) => {
          // -----------------------------------------------
          // CUSTOM CELL VALUE
          // -----------------------------------------------

          if (col.cellTemplate === 'cellValueTemplate') {
            return this.calculateCellValue(row, col);
          }

          // -----------------------------------------------
          // CURRENCY
          // -----------------------------------------------

          if (col.cellTemplate === 'currencyCellTemplate') {
            // IMPORTANT:
            // Keep the actual Excel value as NUMBER.
            //
            // Do NOT use:
            // this.currency.transform(...)
            //
            // because CurrencyPipe returns a STRING.
            return this.getNumericCurrencyValue(row, col);
          }
          // -----------------------------------------------
          // DATE VALUE
          // -----------------------------------------------

          if (col.type === 'date') {
            const value = row[col.field];

            if (!value) {
              return null;
            }

            return new Date(value);
          }
          // -----------------------------------------------
          // NORMAL VALUE
          // -----------------------------------------------

          return row[col.field];
        }),
      );

      // ===================================================
      // APPLY COLUMN FORMATTING
      // ===================================================

      visibleColumns.forEach((col, index) => {
        const cell = dataRow.getCell(index + 1);

        // -----------------------------------------------
        // CURRENCY FORMAT
        // -----------------------------------------------

        if (col.cellTemplate === 'currencyCellTemplate') {
          // Keep the value numeric
          cell.value = this.getNumericCurrencyValue(row, col);

          // Display currency symbol while retaining
          // numeric Excel value.
          //
          // Example:
          // 50000 -> $50,000.00
          // 50000 -> ₹50,000.00
          // 50000 -> €50,000.00
          //
          // SUM / AVERAGE continue to work.
          cell.numFmt = `${currencySymbol}#,##0.00`;
        }
        if (col.type === 'date') {
          cell.numFmt = this.getDateFormateForExport();
        }
      });

      // ===================================================
      // EXCEL OUTLINE / GROUPING
      // ===================================================

      if (this.groupBy) {
        dataRow.outlineLevel = 1;
      }
    }

    // =====================================================
    // COLUMN WIDTH
    // =====================================================

    visibleColumns.forEach((col, index) => {
      worksheet.getColumn(index + 1).width = this.getExcelColumnWidth(
        worksheet,
        index + 1,
        col.header,
      );
    });

    // =====================================================
    // FREEZE MAIN HEADER
    // =====================================================

    worksheet.views = [
      {
        state: 'frozen',
        ySplit: 1,
      },
    ];

    // =====================================================
    // OUTLINE SETTINGS
    // =====================================================

    if (this.groupBy) {
      worksheet.properties.outlineProperties = {
        summaryBelow: true,
        summaryRight: true,
      };
    }

    // =====================================================
    // WRITE FILE
    // =====================================================

    workbook.xlsx.writeBuffer().then((buffer) => {
      this.saveAsExcelFile(buffer, 'products');
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

  getExcelColumnWidth(worksheet: any, columnIndex: number, header: string): number {
    let maxLength = header.length;

    worksheet.getColumn(columnIndex).eachCell((cell: any) => {
      const value = cell.value;

      if (value !== null && value !== undefined) {
        maxLength = Math.max(maxLength, String(value).length);
      }
    });
    return Math.min(Math.max(maxLength + 2, 12), 40);
  }

  private getNumericCurrencyValue(row: any, col: GridColumn): number | null {
    const value = row[col.field];

    if (value === null || value === undefined || value === '') {
      return null;
    }

    const numericValue = Number(value);

    return Number.isNaN(numericValue) ? null : numericValue;
  }

  private getCurrencySymbol(): string {
    const transformed = this.currencyPipe.transform(0, this.currencyCode, 'symbol', '1.0-0');

    if (!transformed) {
      return this.currencyCode;
    }

    // Remove the formatted numeric part.
    return transformed.replace(/[\d\s.,-]+$/, '').trim();
  }

  onColReorder(event: any) {
    const moved = this.columns.splice(event.dragIndex, 1)[0];
    this.columns.splice(event.dropIndex, 0, moved);
    this.visibleColumnsSubject.next(this.columns.filter((col) => col.visible === true));
  }

  getGroupColumnHeader() {
    return this.columns.filter((col) => col.field === this.groupBy)[0].header;
  }

  getDateFormate(): string {
    return this.dateFormat;
  }

  getDateFormateForExport(): string {
    return this.dateFormat.replace(/\//g, '"/"');
  }

  private resetGrid(): void {
    /*
     * Reset the selected rows when filters change.
     */
    this.selectedRows = [];
    /*
     * Reset the page to the first page when filters change.
     */
    this.skip = 0;
    /*
     * Reset the number of items per page when filters change.
     */
    this.take = this.take ?? 25;
    /*
     * Reset the first index when filters change.
     */
    if (this.paginator) {
      this.paginator.first.set(0);
    }
  }
}
