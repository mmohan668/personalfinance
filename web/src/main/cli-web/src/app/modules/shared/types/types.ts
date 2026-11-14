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
  totalAmount: number;
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

export interface UserPermissionViewDto {
  id: number;
  userId: number;
  moduleId: number;
  screenId: number;
  actionId: number;
  hasPermission: boolean;
  moduleName: string;
  screenName: string;
  actionName: string;
}

export interface AppPermission {
  actionName: string;
  moduleName: string;
  screenName: string;
  hasPermission: boolean;
}

export interface UserPermissionUpdateRequest {
  userId: number;
  permissions: { id: number; hasPermission: boolean }[];
}

export interface ToolbarConfig {
  addRow: boolean | false;
  copyRow: boolean | false;
  deleteRow: boolean | false;
  editRow: boolean | false;
  activate: boolean | false;
  inactivate: boolean | false;
  permissions: boolean | false;
  refresh: boolean | false;
  gridSettings: boolean | true;
  export: boolean | true;
  columnChooser: boolean | true;
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
  countryCode: string;
  stateCode: string;
  timeZone: string;
}

export interface SuggestionRequest {
  entity: string;
  field: string;
  value: string;
  dependentField?: string;
  dependentValue?: string;
  limit?: number;
}

export interface DashboardSummary {
  transactions: TransactionTotals;
  expenses: ExpenseByCategory[];
  monthlyTransactions: any[];
}

export interface TransactionTotals {
  expense: number;
  income: number;
  investment: number;
  transfer: number;
}

export interface ExpenseByCategory {
  categoryName: string;
  amount: number;
}

export interface UserProfile {
  username: string;
  email: string;
  displayName: string;
  id: number | null;
  adminUserId: number | null;
  profilePictureUrl: string | null;
}

export interface CaptchaResponse {
  captchaId: string;
  captchaImage: string;
}

export interface ForgotPasswordRequest {
  username: string;
  displayName: string;
  email: string;
  captchaId: string;
  captchaAnswer: string;
}
