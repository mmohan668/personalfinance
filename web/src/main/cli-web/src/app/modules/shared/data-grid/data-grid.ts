import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, Input, OnInit, signal, ViewChild } from '@angular/core';
import { SelectItem, SortEvent, SortMeta } from 'primeng/api';
import { Table } from 'primeng/table';
import { Paginator } from 'primeng/paginator';
import { firstValueFrom } from 'rxjs';
import {
  ApiResponse,
  GridColumn,
  GridFilter,
  GridPersonalizationDto,
  GridSort,
  SearchCriteria,
  ToolbarConfig,
} from '../types/types';
import { GridService } from '../service/grid-service';
import { GRID_NAMES, SORT_ICONS, SORT_ORDERS, TRANSACTION_TYPES } from '../enums';
import * as FileSaver from 'file-saver';
import { CommonService } from '../service/common-service';
import { CommonImportsModule } from '../common-imports/common-imports-module';
import { ColumnFilterComponent } from '../column-filter/column-filter';
import { AppConfigService } from '../service/app-config-service';
import { NotificationService } from '../service/notification-service';
import { GridStateService } from '../service/grid-state-service';
import { EXCEL_EXTENSION, EXCEL_TYPE, EXPORT_DELEMETER } from '../constants';
import { SystemConfigService } from '../service/system-config-service';

@Component({
  selector: 'app-data-grid',
  standalone: true,
  imports: [CommonImportsModule, ColumnFilterComponent],
  styleUrl: './data-grid.scss',
  templateUrl: './data-grid.html',
})
export class DataGrid implements OnInit {
  @ViewChild('dataTable') private dataTable!: Table;
  @ViewChild('paginator') private paginator!: Paginator;

  @Input()
  public addRow!: () => void;
  @Input()
  public copyRow!: () => void;
  @Input()
  public editRow!: () => void;
  @Input()
  public deleteRow!: () => void;
  @Input()
  public activate!: () => void;
  @Input()
  public inactivate!: () => void;
  @Input({ required: true })
  public toolbarConfig!: ToolbarConfig;
  @Input({ required: true })
  public gridName!: string;
  @Input({ required: true })
  public gridExportFileName!: string;
  @Input({ required: true })
  public dataKey!: string;
  @Input()
  public additionalFilters!: GridFilter[];
  @Input()
  public booleanOptions!: { label: string; value: boolean | null }[];
  @Input()
  public calculateCellValue!: (rowData: any, col: GridColumn) => any;
  @Input()
  public fetchTitle!: (rowData: any, col: GridColumn) => any;
  @Input()
  public groupBy: string | null = null;

  public config = inject(AppConfigService);
  public _cs = inject(CommonService);
  private _gs = inject(GridService);
  private _cp = inject(CurrencyPipe);
  private _ns = inject(NotificationService);
  private _gss = inject(GridStateService);
  private _scs = inject(SystemConfigService);

  private readonly columns = signal<GridColumn[]>([]);
  protected dataSource = signal<any[]>([]);
  protected readonly showGrid = signal(false);

  protected visibleColumns = computed(() =>
    this.columns().filter((column) => column.visible === true),
  );
  protected columnOptions = computed<SelectItem[]>(() =>
    this.columns().map((col) => ({
      label: col.header,
      value: col,
    })),
  );

  protected columnSearch = '';
  public selectedRows: any[] = [];
  protected sortMeta: SortMeta[] = [];
  protected sortField = '';
  protected sortOrder: 1 | -1 = 1;
  protected totalRecords = 0;
  protected skip = 0;
  protected take = this.config.configValue.defaultPageSize;
  protected filters: GridFilter[] = [];
  protected currencyCode: string = 'INR';

  constructor() {}

  ngOnInit(): void {
    if (!this.toolbarConfig) {
      this.toolbarConfig = this._cs.toolbarConfig();
    }
    this.loadGridColumns();
  }

  private loadGridColumns(): void {
    const cachedColumns = this._gss.getColumns(this.gridName);
    if (cachedColumns) {
      this.initializeColumns([...cachedColumns]);
      return;
    }
    this.fetchGridColumns();
  }

  private fetchGridColumns(): void {
    this._gs.loadGridColumns(this.gridName).subscribe((columns) => {
      const normalizedColumns = columns.map((column) => ({
        ...column,
        visible: column.visible !== false,
      }));
      this._gss.setColumns(this.gridName, normalizedColumns);
      this.initializeColumns([...normalizedColumns]);
    });
  }

  initializeColumns(columns: GridColumn[]): void {
    this.columns.set(
      columns.map((column) => ({
        ...column,
      })),
    );
    this.initializeDefaultSort(this.columns());
    if (this.additionalFilters?.length) {
      this.filters.push(...this.additionalFilters);
    }
    this.showGrid.set(true);
    this.loadGridData();
  }

  private updateCachedColumns(): void {
    this._gss.setColumns(this.gridName, this.columns());
  }

  loadGridData(): void {
    const sorts: GridSort[] = this.getSorts();
    const request: SearchCriteria = {
      sortList: sorts,
      filterList: this.filters,
      skip: this.skip,
      take: this.take,
      loadAllData: false,
      gridName: this.gridName,
    };
    this._gs.loadGridData(request).subscribe({
      next: (result) => {
        this.dataSource.set(result.recordDetails);
        this.totalRecords = result.totalRecords;
      },
      error: (error) => {
        console.error('Error loading grid data:', error);
        this.dataSource.set([]);
        this.totalRecords = 0;
      },
    });
  }

  private getSorts(): GridSort[] {
    return this.sortMeta
      .filter(
        (sort): sort is SortMeta =>
          !!sort && typeof sort.field === 'string' && (sort.order === 1 || sort.order === -1),
      )
      .map((sort) => ({
        field: sort.field,
        order: sort.order === 1 ? 'asc' : 'desc',
      }));
  }

  initializeDefaultSort(columns: GridColumn[]): void {
    const defaultSortColumns = columns
      .filter((column) => column.sortable !== false && column.defaultSortOrder)
      .sort(
        (a, b) =>
          (a.sortIndex ?? Number.MAX_SAFE_INTEGER) - (b.sortIndex ?? Number.MAX_SAFE_INTEGER),
      );
    if (!defaultSortColumns.length) {
      this.sortMeta = [];
      this.sortField = '';
      this.sortOrder = 1;
      return;
    }
    this.sortMeta = defaultSortColumns.map((column) => ({
      field: column.field,
      order: column.defaultSortOrder === SORT_ORDERS.DESCENDING ? -1 : 1,
    }));
    this.sortField = this.sortMeta[0].field;
    this.sortOrder = this.sortMeta[0].order as any;
  }

  onSort(event: SortEvent): void {
    const eventData = event as SortEvent & {
      multisortmeta?: SortMeta[];
    };
    const multiSortMeta = eventData.multisortmeta ?? eventData.multiSortMeta ?? [];
    if (multiSortMeta.length > 0) {
      this.sortMeta = multiSortMeta.map((sort) => ({
        field: sort.field,
        order: sort.order === -1 ? -1 : 1,
      }));
      this.sortField = this.sortMeta[0]?.field ?? '';
      this.sortOrder = this.sortMeta[0]?.order === -1 ? -1 : 1;
    } else {
      this.sortMeta = [];
      this.sortField = '';
      this.sortOrder = 1;
    }
    this.resetGrid();
    this.loadGridData();
  }

  getSortIcon(field: string): string {
    const sort = this.sortMeta.find((item) => item.field === field);
    if (!sort) {
      return '';
    }
    return sort.order === 1 ? SORT_ICONS.ASCENDING : SORT_ICONS.DESCENDING;
  }

  onPage(event: any): void {
    const loadData = this.skip !== event.first || this.take !== event.rows;
    this.skip = event.first ?? 0;
    this.take = event.rows ?? 5;
    if (loadData) {
      this.loadGridData();
    }
  }

  changeOperator(gridFilter: GridFilter): void {
    this.filters = this.filters.filter((filter) => filter.field !== gridFilter.field);
    if (
      gridFilter.operator === 'isNull' ||
      gridFilter.operator === 'isNotNull' ||
      this._cs.isNotNull(gridFilter.value)
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
      this._cs.isNotNull(gridFilter.value)
    ) {
      this.filters.push(gridFilter);
    }
    this.resetGrid();
    this.loadGridData();
  }

  toggleColumn = (col: GridColumn, checked: boolean): void => {
    this.columns.update((columns) =>
      columns.map((column) =>
        column.field === col.field ? { ...column, visible: checked } : column,
      ),
    );
    this.updateCachedColumns();
  };

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

  allRowsSelected(): boolean {
    const rows = this.dataSource();
    if (rows.length === 0) {
      return false;
    }
    return rows.every((row) => this.isRowSelected(row));
  }

  someRowsSelected(): boolean {
    const rows = this.dataSource();
    if (rows.length === 0) {
      return false;
    }
    const selectedCount = rows.filter((row) => this.isRowSelected(row)).length;
    return selectedCount > 0 && selectedCount < rows.length;
  }

  toggleAllRows(checked: boolean): void {
    const rows = this.dataSource();
    if (checked) {
      const selectedMap = new Map(this.selectedRows.map((row) => [row.id, row]));
      for (const row of rows) {
        selectedMap.set(row.id, row);
      }
      this.selectedRows = Array.from(selectedMap.values());
      return;
    }
    const rowIds = new Set(rows.map((row) => row.id));
    this.selectedRows = this.selectedRows.filter((row) => !rowIds.has(row.id));
  }

  getColumns = (searchInput: string): GridColumn[] => {
    const columns = this.columns();
    if (searchInput !== undefined && searchInput !== null && searchInput !== '') {
      return columns.filter((col) => col.header.toLowerCase().includes(searchInput.toLowerCase()));
    }
    return columns;
  };

  allColumnsSelected = (): boolean => {
    const columns = this.columns();
    return columns.length > 0 && columns.every((col) => col.visible !== false);
  };

  someColumnsSelected = (): boolean => {
    const columns = this.columns();
    const selectedCount = columns.filter((col) => col.visible !== false).length;
    return selectedCount > 0 && selectedCount < columns.length;
  };

  toggleAllColumns = (checked: boolean): void => {
    this.columns.update((columns) =>
      columns.map((col) => ({
        ...col,
        visible: checked,
      })),
    );
    this.updateCachedColumns();
  };

  exportExcel = async (selectionType: string): Promise<void> => {
    const module = await import('exceljs');
    const ExcelJS = module.default ?? module;
    const visibleColumns = this.visibleColumns();
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
        gridName: this.gridName,
      };
      sourceRows = (await firstValueFrom(this._gs.loadGridData(request))).recordDetails;
    }

    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('data');
    const currencySymbol = this.getCurrencySymbol();
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

    let previousGroup: any = Symbol('initial');

    for (const row of sourceRows) {
      const currentGroup = this.groupBy ? row[this.groupBy] : undefined;
      if (this.groupBy && currentGroup !== previousGroup) {
        const groupHeaderRow = worksheet.addRow([
          `${this.getGroupColumnHeader()}: ${currentGroup}`,
        ]);

        if (visibleColumns.length > 1) {
          worksheet.mergeCells(
            groupHeaderRow.number,
            1,
            groupHeaderRow.number,
            visibleColumns.length,
          );
        }

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

      const dataRow = worksheet.addRow(
        visibleColumns.map((col) => {
          if (col.cellTemplate === 'cellValueTemplate') {
            return this.calculateCellValue(row, col);
          }
          if (col.cellTemplate === 'currencyCellTemplate') {
            return this.getNumericCurrencyValue(row, col);
          }
          if (col.dataType === 'date' || col.dataType === 'datetime') {
            const value = row[col.field];
            if (!value) {
              return null;
            }
            const formatted = new DatePipe('en-GB').transform(value, this._scs.dateTimeFormat());
            return formatted ? formatted : null;
          }
          return row[col.field];
        }),
      );

      visibleColumns.forEach((col, index) => {
        const cell = dataRow.getCell(index + 1);
        if (col.cellTemplate === 'currencyCellTemplate') {
          cell.value = this.getNumericCurrencyValue(row, col);
          cell.numFmt = `${currencySymbol}#,##0.00`;
        }
        if (col.dataType === 'date') {
          cell.numFmt = this.getDateFormateForExport();
        } else if (col.dataType === 'datetime') {
          cell.numFmt = this.getDateTimeFormateForExport();
        }
      });
      if (this.groupBy) {
        dataRow.outlineLevel = 1;
      }
    }

    visibleColumns.forEach((col, index) => {
      worksheet.getColumn(index + 1).width = this.getExcelColumnWidth(
        worksheet,
        index + 1,
        col.header,
      );
    });

    worksheet.views = [
      {
        state: 'frozen',
        ySplit: 1,
      },
    ];

    if (this.groupBy) {
      worksheet.properties.outlineProperties = {
        summaryBelow: true,
        summaryRight: true,
      };
    }

    workbook.xlsx.writeBuffer().then((buffer) => {
      this.saveAsExcelFile(buffer, this.gridExportFileName);
    });
  };

  saveAsExcelFile(buffer: any, fileName: string): void {
    const data: Blob = new Blob([buffer], {
      type: EXCEL_TYPE,
    });
    FileSaver.saveAs(data, fileName + EXPORT_DELEMETER + new Date().getTime() + EXCEL_EXTENSION);
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
    const transformed = this._cp.transform(0, this.currencyCode, 'symbol', '1.0-0');
    if (!transformed) {
      return this.currencyCode;
    }
    return transformed.replace(/[\d\s.,-]+$/, '').trim();
  }

  onColReorder(event: any): void {
    this.columns.update((columns) => {
      const next = [...columns];
      const [moved] = next.splice(event.dragIndex, 1);
      next.splice(event.dropIndex, 0, moved);
      return next;
    });
    this.updateCachedColumns();
  }

  getGroupColumnHeader(): string {
    return this.columns().find((col) => col.field === this.groupBy)?.header ?? '';
  }

  getDateFormate(): string {
    return this._scs.dateFormat();
  }

  getDateTimeFormate(): string {
    return this._scs.dateTimeFormat();
  }

  getDateFormateForExport(): string {
    return this._scs.dateFormat().replace(/\//g, '"/"');
  }

  getDateTimeFormateForExport(): string {
    return this._scs.dateTimeFormat().replace(/\//g, '"/"');
  }

  private resetGrid(): void {
    this.selectedRows = [];
    this.skip = 0;
    this.take = this.take ?? 25;
    if (this.paginator) {
      this.paginator.first.set(0);
    }
  }

  saveGridSetting = async (): Promise<void> => {
    const columnsToSave = this.columns().map((col) => ({
      ...col,
    }));
    const sorts: GridSort[] = this.getSorts();
    columnsToSave.forEach((col) => {
      const sortIndex = sorts.findIndex((s) => s.field === col.field);
      if (sortIndex !== -1) {
        col.defaultSortOrder = sorts[sortIndex].order;
        col.sortIndex = sortIndex;
      } else {
        col.defaultSortOrder = null;
      }
    });
    const gridPersonalizationDto: GridPersonalizationDto = {
      gridName: this.gridName,
      gridColumnJson: JSON.stringify(columnsToSave),
      userId: 1,
    };
    firstValueFrom(this._gs.saveGridSetting(gridPersonalizationDto)).then((resp: ApiResponse) => {
      if (resp.success) {
        this._gss.setColumns(this.gridName, columnsToSave);
        this.columns.set(columnsToSave.map((col) => ({ ...col })));
        this._ns.success(resp.message);
      } else {
        this._ns.error(resp.message);
      }
    });
  };

  resetGridSettings = async (): Promise<void> => {
    const gridPersonalizationDto: GridPersonalizationDto = {
      gridName: this.gridName,
      gridColumnJson: null,
      userId: 1,
    };
    firstValueFrom(this._gs.resetGridSettings(gridPersonalizationDto)).then((resp: ApiResponse) => {
      if (resp.success) {
        this._gss.clearColumns(this.gridName);
        this._ns.success(resp.message);
        this.fetchGridColumns();
      } else {
        this._ns.error(resp.message);
      }
    });
  };

  clearSelection = (): void => {
    this.selectedRows = [];
  };

  refreshGrid = (): void => {
    this.clearSelection();
    this.loadGridData();
  };

  getTransactionRowClass(row: any): string {
    if (this.gridName !== GRID_NAMES.ALL_TRANSACTIONS_GRID) {
      return '';
    }
    switch (row.transactionType) {
      case TRANSACTION_TYPES.EXPENSE:
        return 'row-expense';
      case TRANSACTION_TYPES.INCOME:
        return 'row-income';
      case TRANSACTION_TYPES.INVESTMENT:
        return 'row-investment';
      case TRANSACTION_TYPES.TRANSFER:
        return 'row-transfer';
      default:
        return '';
    }
  }
}
