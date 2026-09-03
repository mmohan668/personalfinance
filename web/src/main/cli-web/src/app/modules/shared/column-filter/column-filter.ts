import {
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { formatDate } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';

import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { OverlayModule } from '@angular/cdk/overlay';

import { GridColumn, GridFilter } from '../types/types';

import { COLUMN_TYPES, FILTER_ICONS, FILTER_LABLES, FILTER_OPERATORS } from '../enums';

export interface FilterOperator {
  value: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-column-filter',
  standalone: true,
  imports: [
    InputTextModule,
    MatFormFieldModule,
    MatInputModule,
    OverlayModule,
    FormsModule,
    DatePickerModule,
  ],
  templateUrl: './column-filter.html',
  styleUrl: './column-filter.scss',
})
export class ColumnFilterComponent implements OnInit, OnDestroy {
  // ============================================================
  // INPUTS
  // ============================================================

  @Input()
  column!: GridColumn;

  /**
   * Application-wide date format.
   *
   * Examples:
   *   dd/MM/yyyy
   *   MM/dd/yyyy
   *
   * This is NOT the PrimeNG format.
   */
  @Input({ required: true })
  dateFormat!: string;

  // ============================================================
  // OUTPUTS
  // ============================================================

  @Output()
  valueChange = new EventEmitter<GridFilter>();

  @Output()
  operatorChange = new EventEmitter<GridFilter>();

  // ============================================================
  // STATE
  // ============================================================

  operators: FilterOperator[] = [];

  selectedOperator = '';

  filter!: GridFilter;

  menuOpen = false;

  betweenError = '';

  readonly FILTER_OPERATORS = FILTER_OPERATORS;

  @HostBinding('class.between-date-filter')
  get isBetweenDateFilter(): boolean {
    return (
      this.column?.type === COLUMN_TYPES.DATE && this.selectedOperator === FILTER_OPERATORS.BETWEEN
    );
  }
  /**
   * IMPORTANT:
   *
   * These are the actual DatePicker model values.
   *
   * Do NOT bind the PrimeNG DatePicker directly to a getter
   * that creates a new Date every change-detection cycle.
   */
  filterDateValue: Date | null = null;

  filterDateToValue: Date | null = null;

  private readonly valueChangeSubject = new Subject<GridFilter>();

  private readonly destroy$ = new Subject<void>();

  // ============================================================
  // OPERATORS
  // ============================================================

  textOperators: FilterOperator[] = [
    {
      label: FILTER_LABLES.CONTAINS,
      value: FILTER_OPERATORS.CONTAINS,
      icon: FILTER_ICONS.CONTAINS,
    },
    {
      label: FILTER_LABLES.STARTS_WITH,
      value: FILTER_OPERATORS.STARTS_WITH,
      icon: FILTER_ICONS.STARTS_WITH,
    },
    {
      label: FILTER_LABLES.ENDS_WITH,
      value: FILTER_OPERATORS.ENDS_WITH,
      icon: FILTER_ICONS.ENDS_WITH,
    },
    {
      label: FILTER_LABLES.EQUALS,
      value: FILTER_OPERATORS.EQUALS,
      icon: FILTER_ICONS.EQUALS,
    },
    {
      label: FILTER_LABLES.NOT_EQUALS,
      value: FILTER_OPERATORS.NOT_EQUALS,
      icon: FILTER_ICONS.NOT_EQUALS,
    },
    {
      label: FILTER_LABLES.NOT_CONTAINS,
      value: FILTER_OPERATORS.NOT_CONTAINS,
      icon: FILTER_ICONS.NOT_CONTAINS,
    },
    {
      label: FILTER_LABLES.IS_NULL,
      value: FILTER_OPERATORS.IS_NULL,
      icon: FILTER_ICONS.IS_NULL,
    },
    {
      label: FILTER_LABLES.IS_NOT_NULL,
      value: FILTER_OPERATORS.IS_NOT_NULL,
      icon: FILTER_ICONS.IS_NOT_NULL,
    },
  ];

  numericOperators: FilterOperator[] = [
    {
      label: FILTER_LABLES.EQUALS,
      value: FILTER_OPERATORS.EQUALS,
      icon: FILTER_ICONS.EQUALS,
    },
    {
      label: FILTER_LABLES.NOT_EQUALS,
      value: FILTER_OPERATORS.NOT_EQUALS,
      icon: FILTER_ICONS.NOT_EQUALS,
    },
    {
      label: FILTER_LABLES.GREATER_THAN,
      value: FILTER_OPERATORS.GREATER_THAN,
      icon: FILTER_ICONS.GREATER_THAN,
    },
    {
      label: FILTER_LABLES.GREATER_THAN_OR_EQUAL,
      value: FILTER_OPERATORS.GREATER_THAN_OR_EQUAL,
      icon: FILTER_ICONS.GREATER_THAN_OR_EQUAL,
    },
    {
      label: FILTER_LABLES.LESS_THAN,
      value: FILTER_OPERATORS.LESS_THAN,
      icon: FILTER_ICONS.LESS_THAN,
    },
    {
      label: FILTER_LABLES.LESS_THAN_OR_EQUAL,
      value: FILTER_OPERATORS.LESS_THAN_OR_EQUAL,
      icon: FILTER_ICONS.LESS_THAN_OR_EQUAL,
    },
    {
      label: FILTER_LABLES.BETWEEN,
      value: FILTER_OPERATORS.BETWEEN,
      icon: FILTER_ICONS.BETWEEN,
    },
    {
      label: FILTER_LABLES.IS_NULL,
      value: FILTER_OPERATORS.IS_NULL,
      icon: FILTER_ICONS.IS_NULL,
    },
    {
      label: FILTER_LABLES.IS_NOT_NULL,
      value: FILTER_OPERATORS.IS_NOT_NULL,
      icon: FILTER_ICONS.IS_NOT_NULL,
    },
  ];

  booleanOperators: FilterOperator[] = [
    {
      label: FILTER_LABLES.EQUALS,
      value: FILTER_OPERATORS.EQUALS,
      icon: FILTER_ICONS.EQUALS,
    },
    {
      label: FILTER_LABLES.NOT_EQUALS,
      value: FILTER_OPERATORS.NOT_EQUALS,
      icon: FILTER_ICONS.NOT_EQUALS,
    },
    {
      label: FILTER_LABLES.IS_NULL,
      value: FILTER_OPERATORS.IS_NULL,
      icon: FILTER_ICONS.IS_NULL,
    },
    {
      label: FILTER_LABLES.IS_NOT_NULL,
      value: FILTER_OPERATORS.IS_NOT_NULL,
      icon: FILTER_ICONS.IS_NOT_NULL,
    },
  ];

  // ============================================================
  // PRIME NG DATE FORMAT
  // ============================================================

  /**
   * Converts application date format to PrimeNG DatePicker format.
   *
   * Application:
   *   dd/MM/yyyy
   *
   * PrimeNG:
   *   dd/mm/yy
   *
   * Application:
   *   MM/dd/yyyy
   *
   * PrimeNG:
   *   mm/dd/yy
   */
  get primeNgDateFormat(): string {
    return this.toPrimeNgDateFormat(this.dateFormat);
  }

  private toPrimeNgDateFormat(format: string): string {
    if (!format) {
      return 'dd/mm/yy';
    }

    return format.replace(/yyyy|MMMM|MMM|MM|M|dd|d|yy|y/g, (token: string): string => {
      switch (token) {
        // Angular/application year
        case 'yyyy':
          return 'yy';

        case 'yy':
          return 'y';

        // Angular/application month
        case 'MMMM':
          return 'MM';

        case 'MMM':
          return 'M';

        case 'MM':
          return 'mm';

        case 'M':
          return 'm';

        // Day
        case 'dd':
          return 'dd';

        case 'd':
          return 'd';

        case 'y':
          return 'y';

        default:
          return token;
      }
    });
  }

  // ============================================================
  // CONSTRUCTOR
  // ============================================================

  constructor(private elementRef: ElementRef) {}

  // ============================================================
  // LIFECYCLE
  // ============================================================

  ngOnInit(): void {
    this.initializeFilter();

    this.operators = this.getOperators(this.column);

    /*
     * Initialize the DatePicker models ONCE.
     *
     * After this, the DatePicker works with the actual Date
     * properties instead of a getter-generated Date.
     */
    this.syncDatePickerValues();

    this.valueChangeSubject
      .pipe(
        debounceTime(500),
        distinctUntilChanged(
          (previous, current) =>
            previous.field === current.field &&
            previous.operator === current.operator &&
            previous.value === current.value &&
            previous.valueTo === current.valueTo,
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((filter) => {
        this.onValueChange(filter);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.valueChangeSubject.complete();
  }

  // ============================================================
  // DATE MODEL
  // ============================================================

  /**
   * Keep the PrimeNG model synchronized with the stored
   * yyyy-MM-dd filter values.
   */
  private syncDatePickerValues(): void {
    if (this.column.type !== COLUMN_TYPES.DATE) {
      this.filterDateValue = null;
      this.filterDateToValue = null;
      return;
    }

    this.filterDateValue = this.parseFilterDate(this.filter?.value);

    this.filterDateToValue = this.parseFilterDate(this.filter?.valueTo);
  }

  /**
   * Convert yyyy-MM-dd string to a local Date.
   *
   * We intentionally do NOT use:
   *
   *   new Date('yyyy-MM-dd')
   *
   * because that can be interpreted as UTC and cause timezone
   * shifts.
   */
  private parseFilterDate(value?: string): Date | null {
    if (!value) {
      return null;
    }

    const parts = value.split('-');

    if (parts.length !== 3) {
      return null;
    }

    const year = Number(parts[0]);
    const month = Number(parts[1]);
    const day = Number(parts[2]);

    if (!Number.isInteger(year) || !Number.isInteger(month) || !Number.isInteger(day)) {
      return null;
    }

    const date = new Date(year, month - 1, day);

    if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
      return null;
    }

    return date;
  }

  /**
   * Convert PrimeNG DatePicker Date into backend yyyy-MM-dd.
   */
  private formatFilterDate(value: Date | null): string {
    if (!value || isNaN(value.getTime())) {
      return '';
    }

    return formatDate(value, 'yyyy-MM-dd', 'en-US');
  }

  // ============================================================
  // DATE PICKER EVENTS
  // ============================================================

  /**
   * Normal/single DatePicker.
   */
  onDateChange(value: Date | null): void {
    this.betweenError = '';

    /*
     * IMPORTANT:
     *
     * Update the DatePicker model itself.
     */
    this.filterDateValue = value;

    /*
     * Store backend value as yyyy-MM-dd.
     */
    this.filter.value = value ? this.formatFilterDate(value) : '';

    this.valueChangeSubject.next({
      ...this.filter,
    });
  }

  /**
   * BETWEEN DatePicker.
   */
  onBetweenDateChange(type: 'from' | 'to', value: Date | null): void {
    this.betweenError = '';

    const filterValue = value ? this.formatFilterDate(value) : '';

    if (type === 'from') {
      /*
       * Update DatePicker model.
       */
      this.filterDateValue = value;

      /*
       * Store yyyy-MM-dd.
       */
      this.filter.value = filterValue;
    } else {
      /*
       * Update DatePicker model.
       */
      this.filterDateToValue = value;

      /*
       * Store yyyy-MM-dd.
       */
      this.filter.valueTo = filterValue;
    }

    /*
     * If both dates are not populated yet, simply wait.
     */
    if (!this.isValidBetween()) {
      return;
    }

    this.valueChangeSubject.next({
      ...this.filter,
    });
  }

  // ============================================================
  // GENERIC INPUT
  // ============================================================

  /**
   * Normal non-date filter input.
   */
  onInput(value: string | Date | null): void {
    this.betweenError = '';

    if (this.column.type === COLUMN_TYPES.DATE) {
      const date = this.toDate(value);

      if (!date) {
        this.filter.value = '';
        this.filterDateValue = null;
        return;
      }

      this.filterDateValue = date;
      this.filter.value = this.formatFilterDate(date);
    } else {
      this.filter.value = value == null ? '' : String(value);
    }

    this.valueChangeSubject.next({
      ...this.filter,
    });
  }

  /**
   * Handle non-DatePicker BETWEEN input.
   */
  onBetweenInput(type: 'from' | 'to', value: string | Date | null): void {
    this.betweenError = '';

    let filterValue: string;

    if (this.column.type === COLUMN_TYPES.DATE) {
      const date = this.toDate(value);

      if (!date) {
        this.betweenError = 'Invalid date';
        return;
      }

      filterValue = this.formatFilterDate(date);

      if (type === 'from') {
        this.filterDateValue = date;
      } else {
        this.filterDateToValue = date;
      }
    } else {
      filterValue = value == null ? '' : String(value);
    }

    if (type === 'from') {
      this.filter.value = filterValue;
    } else {
      this.filter.valueTo = filterValue;
    }

    if (!this.isValidBetween()) {
      return;
    }

    this.valueChangeSubject.next({
      ...this.filter,
    });
  }

  /**
   * Safely convert a value into Date.
   */
  private toDate(value: string | Date | null): Date | null {
    if (!value) {
      return null;
    }

    if (value instanceof Date) {
      return isNaN(value.getTime()) ? null : value;
    }

    /*
     * Stored backend format.
     */
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      return this.parseFilterDate(value);
    }

    const date = new Date(value);

    return isNaN(date.getTime()) ? null : date;
  }

  // ============================================================
  // FILTER INITIALIZATION
  // ============================================================

  initializeFilter(): void {
    if (this.column.filterable) {
      this.filter = {
        field: this.column.field,
        operator: this.getDefaultOperator(this.column),
        value: '',
      };

      this.selectedOperator = this.filter.operator;
    }
  }

  getDefaultOperator(column: GridColumn): string {
    if (column.defaultFilterOperator) {
      return column.defaultFilterOperator;
    }

    switch (column.type) {
      case COLUMN_TYPES.NUMBER:
      case COLUMN_TYPES.DATE:
      case COLUMN_TYPES.BOOLEAN:
        return FILTER_OPERATORS.EQUALS;

      case COLUMN_TYPES.TEXT:
      default:
        return FILTER_OPERATORS.CONTAINS;
    }
  }

  getOperators(column: GridColumn): FilterOperator[] {
    switch (column.type) {
      case COLUMN_TYPES.NUMBER:
      case COLUMN_TYPES.DATE:
        return this.numericOperators;

      case COLUMN_TYPES.BOOLEAN:
        return this.booleanOperators;

      case COLUMN_TYPES.TEXT:
      default:
        return this.textOperators;
    }
  }

  // ============================================================
  // OPERATOR MENU
  // ============================================================

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.menuOpen = false;
    }
  }

  get currentOperator(): FilterOperator | undefined {
    return this.operators.find((operator) => operator.value === this.selectedOperator);
  }

  get operatorSymbol(): string {
    return this.currentOperator?.icon ?? '=';
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  selectOperator(operator: FilterOperator): void {
    this.selectedOperator = operator.value;

    this.menuOpen = false;

    this.filter.operator = operator.value;

    this.betweenError = '';

    if (operator.value === FILTER_OPERATORS.BETWEEN) {
      this.filter.value = '';
      this.filter.valueTo = '';

      /*
       * Clear both DatePicker models too.
       */
      this.filterDateValue = null;
      this.filterDateToValue = null;
    } else if (
      operator.value === FILTER_OPERATORS.IS_NULL ||
      operator.value === FILTER_OPERATORS.IS_NOT_NULL
    ) {
      this.filter.value = '';
      this.filter.valueTo = undefined;

      this.filterDateValue = null;
      this.filterDateToValue = null;
    } else {
      this.filter.valueTo = undefined;

      this.filterDateToValue = null;
    }

    this.operatorChange.emit({
      ...this.filter,
    });
  }

  // ============================================================
  // VALUE CHANGE
  // ============================================================

  onValueChange(gridFilter: GridFilter): void {
    this.valueChange.emit({
      ...gridFilter,
    });
  }

  // ============================================================
  // BETWEEN VALIDATION
  // ============================================================

  private isValidBetween(): boolean {
    this.betweenError = '';

    if (!this.filter.value || !this.filter.valueTo) {
      return false;
    }

    if (this.column.type === COLUMN_TYPES.NUMBER) {
      const from = Number(this.filter.value);
      const to = Number(this.filter.valueTo);

      if (from > to) {
        this.betweenError = 'To value should be greater than or equal to From value';

        return false;
      }
    }

    if (this.column.type === COLUMN_TYPES.DATE) {
      const from = this.parseFilterDate(this.filter.value);

      const to = this.parseFilterDate(this.filter.valueTo);

      if (!from || !to) {
        this.betweenError = 'Invalid date';

        return false;
      }

      if (from.getTime() > to.getTime()) {
        this.betweenError = 'To date should be greater than or equal to From date';

        return false;
      }
    }

    return true;
  }

  // ============================================================
  // RESET
  // ============================================================

  onReset(): void {
    this.betweenError = '';

    this.filter.value = '';

    this.filter.valueTo = undefined;

    this.filter.operator = this.getDefaultOperator(this.column);

    this.selectedOperator = this.filter.operator;

    /*
     * Clear actual DatePicker models.
     */
    this.filterDateValue = null;
    this.filterDateToValue = null;

    this.valueChange.emit({
      ...this.filter,
    });

    this.menuOpen = false;
  }
}
