import { Component, OnInit, ViewChild } from '@angular/core';
import { TableModule, Table } from 'primeng/table';
import { ColumnFilterComponent, FilterOperator } from './../column-filter/column-filter';
import { GridColumn, GridFilter, SearchCriteria } from '../types/types';
import { GridService } from '../service/grid-service';
import { BehaviorSubject } from 'rxjs';
import { AsyncPipe } from '@angular/common';
import { SortEvent } from 'primeng/api';
import { SORT_ICONS, SORT_ORDERS } from '../enums';

@Component({
  imports: [TableModule, ColumnFilterComponent, AsyncPipe],
  selector: 'app-data-grid',
  styleUrl: './data-grid.scss',
  templateUrl: './data-grid.html',
  standalone: true,
})
export class DataGrid implements OnInit {
  @ViewChild('dt') dt!: Table;
  columnsSubject = new BehaviorSubject<GridColumn[]>([]);
  columns$ = this.columnsSubject.asObservable();
  dataSourceSubject = new BehaviorSubject<any[]>([]);
  dataSource$ = this.dataSourceSubject.asObservable();
  filters: GridFilter[] = [];
  sortField = '';
  sortOrder: 1 | -1 = 1;

  constructor(private gridService: GridService) {}

  ngOnInit(): void {
    this.gridService.getColumns().subscribe((columns) => {
      this.columnsSubject.next(columns);
      this.initializeDefaultSort(columns);
      this.getData();
    });
  }

  getData(): void {
    console.log('GET DATA');
    const request: SearchCriteria = {
      sort: {
        field: this.sortField,
        order: this.sortOrder === 1 ? SORT_ORDERS.ASCENDING : SORT_ORDERS.DESCENDING,
      },
      filterList: this.filters,
    };
    this.gridService.getData(request).subscribe((data) => {
      this.dataSourceSubject.next(data);
    });
  }

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

  onSort(event: SortEvent): void {
    this.sortField = event.field ?? '';
    this.sortOrder = event.order === -1 ? -1 : 1;
    this.getData();
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) {
      return '';
    }
    return this.sortOrder === 1 ? SORT_ICONS.ASCENDING : SORT_ICONS.DESCENDING;
  }

  /*
   * Operator changed
   */
  changeOperator(gridFilter: GridFilter): void {
    this.filters.push(gridFilter);
    this.getData();
  }

  /*
   * Apply filter
   */
  applyFilter(gridFilter: GridFilter): void {
    this.filters.push(gridFilter);
    this.getData();
  }
}
