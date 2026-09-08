// grid-column.model.ts
export interface GridColumn {
  field: string;
  header: string;
  dataType: string;
  sortable: boolean;
  defaultSortOrder?: string | null; // optional since it can be null
  filterable: boolean;
  defaultFilterOperator?: string; // optional since it can be null
  width?: number; // optional since it can be null
  align?: 'LEFT' | 'CENTER' | 'RIGHT'; // optional since it can be null
  visible?: boolean;
  visibleIndex?: number;
  cellTemplate?: string;
  sortPriority?: number;
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
  gridName: string;
}

export interface GridPersonalizationDto {
  userId: number;
  gridColumnJson: string;
  gridName: string;
}

export interface ApiResponse {
  success: boolean;
  message: string;
  data: any;
}
