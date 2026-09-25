import { GRID_NAMES } from './enums';

export const GRID_URL_MAP = new Map<string, string>([
  [GRID_NAMES.CATEGORIES_GRID, '/categoryManagement/fetchCategoriesGridData'],
  [GRID_NAMES.SUBCATEGORIES_GRID, '/categoryManagement/fetchSubcategoriesGridData'],
  [GRID_NAMES.REFERENCE_OBJECTS_GRID, '/settings/fetchReferenceObjectGridData'],
  [GRID_NAMES.REFERENCE_VALUES_GRID, '/settings/fetchReferenceValueGridData'],
  [GRID_NAMES.EXPENSES_GRID, '/financialTransaction/financialTransactionGridData'],
  [GRID_NAMES.INCOMES_GRID, '/financialTransaction/financialTransactionGridData'],
  [GRID_NAMES.INVESTMENTS_GRID, '/financialTransaction/financialTransactionGridData'],
  [GRID_NAMES.TRANSFERS_GRID, '/financialTransaction/financialTransactionGridData'],
  [GRID_NAMES.ALL_TRANSACTIONS_GRID, '/financialTransaction/financialTransactionGridData'],
]);

export const SYSTEM = 'SYSTEM';
export const EMPTY = '';
export const EXCEL_TYPE =
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
export const EXCEL_EXTENSION = '.xlsx';
export const EXPORT_DELEMETER = '_export_';
