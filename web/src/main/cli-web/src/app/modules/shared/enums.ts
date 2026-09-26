export enum FILTER_OPERATORS {
  CONTAINS = 'contains',
  NOT_CONTAINS = 'notContains',
  STARTS_WITH = 'startsWith',
  ENDS_WITH = 'endsWith',
  EQUALS = 'equals',
  NOT_EQUALS = 'notEquals',
  GREATER_THAN = 'gt',
  GREATER_THAN_OR_EQUAL = 'gte',
  LESS_THAN = 'lt',
  LESS_THAN_OR_EQUAL = 'lte',
  BETWEEN = 'between',
  IS_NULL = 'isNull',
  IS_NOT_NULL = 'isNotNull',
}

export enum FILTER_LABLES {
  CONTAINS = 'Contains',
  NOT_CONTAINS = 'Not contains',
  STARTS_WITH = 'Starts with',
  ENDS_WITH = 'Ends with',
  EQUALS = 'Equals',
  NOT_EQUALS = 'Not equals',
  GREATER_THAN = 'Greater than',
  GREATER_THAN_OR_EQUAL = 'Greater than or equal',
  LESS_THAN = 'Less than',
  LESS_THAN_OR_EQUAL = 'Less than or equal',
  BETWEEN = 'Between',
  IS_NULL = 'Is null',
  IS_NOT_NULL = 'Is not null',
}

export enum FILTER_ICONS {
  CONTAINS = '⌕',
  NOT_CONTAINS = '⊘',
  STARTS_WITH = '↦',
  ENDS_WITH = '↤',
  EQUALS = '=',
  NOT_EQUALS = '≠',
  GREATER_THAN = '>',
  GREATER_THAN_OR_EQUAL = '≥',
  LESS_THAN = '<',
  LESS_THAN_OR_EQUAL = '≤',
  BETWEEN = '↔',
  IS_NULL = '∅',
  IS_NOT_NULL = '∉',
}

export enum SORT_ORDERS {
  ASCENDING = 'asc',
  DESCENDING = 'desc',
}

export enum SORT_ICONS {
  ASCENDING = '↑',
  DESCENDING = '↓',
}

export enum COLUMN_TYPES {
  TEXT = 'text',
  NUMBER = 'number',
  DATE = 'date',
  DATETIME = 'datetime',
  BOOLEAN = 'boolean',
}

export enum GRID_NAMES {
  CATEGORIES_GRID = 'CATEGORIES_GRID',
  SUBCATEGORIES_GRID = 'SUBCATEGORIES_GRID',
  REFERENCE_OBJECTS_GRID = 'REFERENCE_OBJECTS_GRID',
  REFERENCE_VALUES_GRID = 'REFERENCE_VALUES_GRID',
  EXPENSES_GRID = 'EXPENSES_GRID',
  INCOMES_GRID = 'INCOMES_GRID',
  INVESTMENTS_GRID = 'INVESTMENTS_GRID',
  TRANSFERS_GRID = 'TRANSFERS_GRID',
  ALL_TRANSACTIONS_GRID = 'ALL_TRANSACTIONS_GRID',
}

export enum GRID_EXPORT_FILE_NAMES {
  CATEGORIES_EFN = 'categories',
  SUBCATEGORIES_EFN = 'subcategories',
  REFERENCE_OBJECTS_EFN = 'reference_objects',
  REFERENCE_VALUES_EFN = 'reference_values',
  EXPENSES_GRID_EFN = 'expense_transactions',
  INCOMES_GRID_EFN = 'income_transactions',
  INVESTMENTS_GRID_EFN = 'investment_transactions',
  TRANSFERS_GRID_EFN = 'transfer_transactions',
  ALL_TRANSACTIONS_GRID_EFN = 'all_transactions',
}

export enum DATA_FIELDS {
  ID = 'id',
  USER_ID = 'user.id',
  TRANSACTION_TYPE = 'transactionType',
  LOCATION = 'location',
  LOCATION_DESCRIPTION = 'locationDescription',
  transaction_type_ID = 'transactionTypeId',
  CATEGORY_NAME = 'categoryName',
  CATEGORY_DESCRIPTION = 'categoryDescription',
  REF_OBJ_NAME_ID = 'refObjNameId',
  REFERENCE_CODE = 'referenceCode',
  REFERENCE_CODE_2 = 'referenceCode2',
  REFERENCE_CODE_3 = 'referenceCode3',
  REFERENCE_CODE_DESCRIPTION = 'referenceCodeDescription',
  TRANSACTION_AT = 'transactionAt',
  AMOUNT = 'amount',
  TRANSACTION_TYPE_ID = 'transactionTypeId',
  CATEGORY_ID = 'categoryId',
  SUBCATEGORY_ID = 'subcategoryId',
  LOCATION_ID = 'locationId',
  REMARKS = 'remarks',
  USER_CATEGORY_ID = 'userCategoryId',
  CATEGORY = 'category',
  SUBCATEGORY_NAME = 'subcategoryName',
  SUBCATEGORY_DESCRIPTION = 'subcategoryDescription',
  IS_ACTIVE = 'isActive',
}

export enum MODES {
  ADD = 'Add',
  COPY = 'Copy',
  EDIT = 'Edit',
  DELETE = 'Delete',
}

export enum TRANSACTION_TYPES {
  EXPENSE = 'EXPENSE',
  INCOME = 'INCOME',
  INVESTMENT = 'INVESTMENT',
  TRANSFER = 'TRANSFER',
}

export enum REF_OBJ_NAMES {
  TRANSACTION_TYPE = 'TRANSACTION_TYPE',
  CURRENCY_CODE = 'CURRENCY_CODE',
  LOCATION = 'LOCATION',
}

export enum STATUS {
  ACTIVE = 'Active',
  INACTIVE = 'Inactive',
}
