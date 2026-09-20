import { GRID_NAMES } from './enums';

export const GRID_URL_MAP = new Map<string, string>([
  [GRID_NAMES.CATEGORIES_GRID, '/categoryManagement/fetchCategoriesGridData'],
  [GRID_NAMES.SUBCATEGORIES_GRID, '/categoryManagement/fetchSubcategoriesGridData'],
  [GRID_NAMES.REFERENCE_OBJECTS_GRID, '/settings/fetchReferenceObjectGridData'],
  [GRID_NAMES.REFERENCE_VALUES_GRID, '/settings/fetchReferenceValueGridData'],
  [GRID_NAMES.EXPENSES_GRID, '/financialTransaction/fetchExpensesGridData'],
]);

export const SYSTEM = 'SYSTEM';
export const EMPTY = '';
