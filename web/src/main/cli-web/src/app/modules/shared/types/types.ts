export interface GridColumn {
  field: string;
  header: string;
  dataType: string;
  sortable: boolean;
  defaultSortOrder?: string | null;
  filterable: boolean;
  defaultFilterOperator?: string;
  width?: number;
  align?: 'LEFT' | 'CENTER' | 'RIGHT';
  visible?: boolean;
  visibleIndex?: number;
  cellTemplate?: string;
  sortIndex?: number;
  exportable: boolean;
}

export interface ExportRequest {
  userId: any;
  exportFileName: string;
  searchCriteria: SearchCriteria;
  columns: GridColumn[];
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
  gridColumnJson: string | null;
  gridName: string;
}

export interface ApiResponse {
  success: boolean;
  message: string;
  data: any;
}

export interface ToolbarConfig {
  addRow: boolean | false;
  copyRow: boolean | false;
  deleteRow: boolean | false;
  editRow: boolean | false;
  activate: boolean | false;
  inactivate: boolean | false;
  refresh: boolean | false;
}

export interface SelectItem {
  value: any;
  label: string;
}

export interface ConfirmationDialogData {
  title: string;
  message: string;
  isNotification: boolean | false;
}

export interface FinancialTransactionDto {
  id: number | null;
  amount: number;
  transactionAt: any;
  transactionTypeId: number;
  categoryId: number;
  subcategoryId: number;
  locationId: number;
  remarks: string;
}

export interface SystemConfigValue {
  currencyCode: string;
  locale: string;
  dateFormat: string;
}

export interface SuggestionRequest {
  entity: string;
  field: string;
  value: string;
  dependentField?: string;
  dependentValue?: string;
  limit?: number;
}
