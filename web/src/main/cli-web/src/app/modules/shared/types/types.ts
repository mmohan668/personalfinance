export interface GridColumn {
  field: string;
  header: string;
  type: 'text' | 'number' | 'date' | 'boolean';
  sortable?: boolean;
  defaultSortOrder?: 'asc' | 'desc';
  filterable?: boolean;
  filterType: 'text' | 'number';
  defaultFilterOperator?: string;
  width?: string;
  align?: 'left' | 'center' | 'right';
  visible?: boolean;
}

export interface GridFilter {
  operator: string;
  value: any;
}

export interface FilterOperator {
  label: string;
  value: string;
  symbol: string;
  icon: string;
}

export interface GridRow {
  [key: string]: unknown;
}
