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
  BOOLEAN = 'boolean',
}
