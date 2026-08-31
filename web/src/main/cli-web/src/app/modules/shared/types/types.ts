export interface GridColumn {
  field: string;

  header: string;

  type: 'text' | 'number' | 'date' | 'boolean';

  sortable?: boolean;

  defaultSortOrder?: 'asc' | 'desc';

  filterable?: boolean;

  defaultFilterOperator?: string;

  width?: string;

  align?: 'left' | 'center' | 'right';

  visible?: boolean;
  cellTemplate: string;
}

export interface GridFilter {
  field: string;

  operator: string;

  value: any;
  valueTo?: any;
}

export interface GridSort {
  field: string;

  order: 'asc' | 'desc';
}

export interface GridResult {
  recordDetails: any[];

  totalRecords: number;
}

export interface FilterOperator {
  label: string;

  value: string;

  symbol: string;

  icon: string;
}

export interface SearchCriteria {
  sortList: GridSort[];

  filterList: GridFilter[];

  skip: number;

  take: number;

  loadAllData: boolean | false;
}
