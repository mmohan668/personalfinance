import { Component, OnInit, ViewChild } from '@angular/core';
import { TableModule, ColumnFilter, SortIcon, Table } from 'primeng/table';
import { ColumnFilterComponent, FilterOperator } from './../column-filter/column-filter';
import { GridColumn, GridFilter, GridRow } from '../types/types';

@Component({
  imports: [TableModule, ColumnFilter, SortIcon, ColumnFilterComponent],
  selector: 'app-data-grid',
  styleUrl: './data-grid.scss',
  templateUrl: './data-grid.html',
  standalone: true,
})
export class DataGrid implements OnInit {
  @ViewChild('dt') dt!: Table;

  ngOnInit(): void {
    this.initializeFilters();
    this.initializeDefaultSort();
  }

  columns: GridColumn[] = [
    {
      field: 'id',
      header: 'ID',
      type: 'number',
      sortable: false,
      filterable: false,
      filterType: 'number',
      defaultFilterOperator: 'equals',
      width: '80px',
      align: 'right',
      visible: true,
    },
    {
      field: 'name',
      header: 'Name',
      type: 'text',
      sortable: true,
      defaultSortOrder: 'asc',
      filterable: true,
      filterType: 'text',
      defaultFilterOperator: 'contains',
      visible: true,
    },
    {
      field: 'category',
      header: 'Category',
      type: 'text',
      sortable: true,
      filterable: true,
      filterType: 'text',
      defaultFilterOperator: 'contains',
      visible: true,
    },
    {
      field: 'price',
      header: 'Price (₹)',
      type: 'number',
      sortable: true,
      filterable: true,
      filterType: 'number',
      defaultFilterOperator: 'equals',
      align: 'right',
      visible: true,
    },
    {
      field: 'quantity',
      header: 'Quantity',
      type: 'number',
      sortable: true,
      filterable: true,
      filterType: 'number',
      defaultFilterOperator: 'equals',
      align: 'right',
      visible: true,
    },
  ];

  textOperators: FilterOperator[] = [
    { label: 'Contains', value: 'contains', icon: '⌕' },
    { label: 'Starts with', value: 'startsWith', icon: '↦' },
    { label: 'Ends with', value: 'endsWith', icon: '↤' },
    { label: 'Equals', value: 'equals', icon: '=' },
    { label: 'Not equals', value: 'notEquals', icon: '≠' },
    { label: 'Not contains', value: 'notContains', icon: '⊘' },
    { label: 'Is null', value: 'isNull', icon: '∅' },
    { label: 'Is not null', value: 'isNotNull', icon: '∉' },
  ];

  numericOperators: FilterOperator[] = [
    { label: 'Equals', value: 'equals', icon: '=' },
    { label: 'Not equals', value: 'notEquals', icon: '≠' },
    { label: 'Greater than', value: 'gt', icon: '>' },
    { label: 'Greater than or equal', value: 'gte', icon: '≥' },
    { label: 'Less than', value: 'lt', icon: '<' },
    { label: 'Less than or equal', value: 'lte', icon: '≤' },
    { label: 'Is null', value: 'isNull', icon: '∅' },
    { label: 'Is not null', value: 'isNotNull', icon: '∉' },
  ];

  getOperators(column: GridColumn) {
    switch (column.type) {
      case 'number':
        return this.numericOperators;
      case 'date':
        return this.numericOperators;
      case 'boolean':
        return [
          { label: 'Equals', value: 'equals', icon: '=' },
          { label: 'Not equals', value: 'notEquals', icon: '≠' },
          { label: 'Reset', value: 'reset', icon: '↻' },
        ];
      case 'text':
      default:
        return this.textOperators;
    }
  }

  filters: Record<string, GridFilter> = {};

  initializeFilters(): void {
    this.filters = {};
    for (const column of this.columns) {
      if (!column.filterable) {
        continue;
      }
      this.filters[column.field] = {
        value: '',
        operator: this.getDefaultOperator(column),
      };
    }
  }

  getDefaultOperator(column: GridColumn): string {
    if (column.defaultFilterOperator) {
      return column.defaultFilterOperator;
    }
    switch (column.type) {
      case 'number':
        return 'equals';
      case 'date':
        return 'equals';
      case 'boolean':
        return 'equals';
      case 'text':
      default:
        return 'contains';
    }
  }

  products: GridRow[] = [
    {
      id: 1,
      name: 'Laptop',
      category: 'Electronics',
      price: 75000,
      quantity: 10,
    },
    {
      id: 2,
      name: 'Mobile Phone',
      category: 'Electronics',
      price: 35000,
      quantity: 25,
    },
    {
      id: 3,
      name: 'Keyboard',
      category: 'Accessories',
      price: 2500,
      quantity: 50,
    },
    {
      id: 4,
      name: 'Monitor',
      category: 'Electronics',
      price: 18000,
      quantity: 15,
    },
    {
      id: 5,
      name: 'Mouse',
      category: 'Accessories',
      price: 1200,
      quantity: 100,
    },
  ];

  sortField = '';
  sortOrder: 1 | -1 = 1;

  initializeDefaultSort(): void {
    const defaultSortColumn = this.columns.find(
      (column) => column.sortable !== false && column.defaultSortOrder !== undefined,
    );

    if (!defaultSortColumn) {
      return;
    }

    this.sortField = defaultSortColumn.field;

    this.sortOrder = defaultSortColumn.defaultSortOrder === 'desc' ? -1 : 1;

    this.applyDefaultSort();
  }

  applyDefaultSort(): void {
    if (!this.sortField) {
      return;
    }

    this.products = [...this.products].sort((a, b) => {
      const valueA = a[this.sortField];
      const valueB = b[this.sortField];

      if (valueA == null && valueB == null) {
        return 0;
      }

      if (valueA == null) {
        return -1 * this.sortOrder;
      }

      if (valueB == null) {
        return 1 * this.sortOrder;
      }

      return (
        String(valueA).localeCompare(String(valueB), undefined, {
          numeric: true,
          sensitivity: 'base',
        }) * this.sortOrder
      );
    });
  }

  onSort(event: any): void {
    this.sortField = event.field;
    this.sortOrder = event.order;
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) {
      return '';
    }

    return this.sortOrder === 1 ? '↑' : '↓';
  }

  /*
   * Column filter
   */
  applyFilter(field: keyof typeof this.filters, value: string): void {
    this.filters[field].value = value;

    const operator = this.filters[field].operator;
    if (operator === 'isNull') {
      this.dt.filter(null, field, operator);
      // Force empty result if no nulls exist
      this.products = this.products.filter((row) => row[field] == null);
      return;
    }

    if (operator === 'isNotNull') {
      this.dt.filter(null, field, operator);
      // Force all rows if all values are non-null
      this.products = this.products.filter((row) => row[field] != null);
      return;
    }

    const filterValue =
      value === '' ? null : ['id', 'price', 'quantity'].includes(field) ? Number(value) : value;

    this.dt.filter(filterValue, field, this.filters[field].operator);
  }

  /*
   * Operator changed
   */
  changeOperator(field: keyof typeof this.filters, operator: string): void {
    this.filters[field].operator = operator;

    if (operator === 'isNull' || operator === 'isNotNull') {
      this.filters[field].value = '';
    }
    /*
     * Re-apply the existing value
     * using the new operator.
     */
    this.applyFilter(field, this.filters[field].value);
  }
}
