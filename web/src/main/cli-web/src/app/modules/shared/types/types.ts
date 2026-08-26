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
  field: string;
  operator: string;
  value: any;
}

export interface GridSort {
  field: string;
  order: 'asc' | 'desc';
}

export interface FilterOperator {
  label: string;
  value: string;
  symbol: string;
  icon: string;
}

export interface SearchCriteria {
  sort: GridSort;
  filterList: GridFilter[];
}
