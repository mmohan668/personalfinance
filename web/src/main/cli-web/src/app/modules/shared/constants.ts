import { GRID_NAMES } from './enums';

export const GRID_URL_MAP = new Map<string, string>([
  [GRID_NAMES.CATEGORIES_GRID, '/categoryManagement/fetchCategoriesGridData'],
  [GRID_NAMES.SUBCATEGORIES_GRID, '/categoryManagement/fetchSubcategoriesGridData'],
  [GRID_NAMES.REFERENCE_OBJECTS_GRID, '/settings/fetchReferenceObjectGridData'],
  [GRID_NAMES.REFERENCE_VALUES_GRID, '/settings/fetchReferenceValueGridData'],
  [GRID_NAMES.EXPENSES_GRID, '/financialTransaction/fetchFinancialTransactionGridData'],
  [GRID_NAMES.INCOMES_GRID, '/financialTransaction/fetchFinancialTransactionGridData'],
  [GRID_NAMES.INVESTMENTS_GRID, '/financialTransaction/fetchFinancialTransactionGridData'],
  [GRID_NAMES.TRANSFERS_GRID, '/financialTransaction/fetchFinancialTransactionGridData'],
  [GRID_NAMES.ALL_TRANSACTIONS_GRID, '/financialTransaction/fetchFinancialTransactionGridData'],
  [GRID_NAMES.SYSTEM_CONFIG, '/settings/fetchSystemConfigGridData'],
  [GRID_NAMES.BULK_UPLOADS_STATUS, '/bulkupload/fetchBulkUploadStatusGridData'],
]);

export const SYSTEM = 'PERSONALFINANCEAPP';
export const EMPTY = '';
export const EXCEL_TYPE =
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
export const EXCEL_EXTENSION = '.xlsx';
export const EXPORT_DELEMETER = '_export_';
export const TIME_FORMAT = ' HH:mm:ss';
export const DEFAULT_DATE_FORMAT = 'dd/MM/yyyy';
export const DEFAULT_DATE_TIME_FORMAT = 'dd/MM/yyyy HH:mm:ss';
export const DEFAULT_CURRENCY_CODE = 'INR';
export const DEFAULT_CURRENCY_LOCALE = 'en-IN';
