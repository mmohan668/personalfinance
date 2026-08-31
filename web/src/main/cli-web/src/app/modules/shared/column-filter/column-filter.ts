import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { GridColumn, GridFilter } from '../types/types';
import { COLUMN_TYPES, FILTER_ICONS, FILTER_LABLES, FILTER_OPERATORS } from '../enums';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { OverlayModule } from '@angular/cdk/overlay';

export interface FilterOperator {
  value: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-column-filter',
  standalone: true,
  imports: [InputTextModule, MatFormFieldModule, MatInputModule, OverlayModule],
  templateUrl: './column-filter.html',
  styleUrl: './column-filter.scss',
})
export class ColumnFilterComponent implements OnInit, OnDestroy {
  operators: FilterOperator[] = [];
  selectedOperator = '';
  filter!: GridFilter;
  @Output()
  valueChange = new EventEmitter<GridFilter>();
  @Output()
  operatorChange = new EventEmitter<GridFilter>();
  /**
   * Grid column configuration.
   */
  @Input()
  column!: GridColumn;
  menuOpen = false;
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
    { label: FILTER_LABLES.EQUALS, value: FILTER_OPERATORS.EQUALS, icon: FILTER_ICONS.EQUALS },
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
    { label: FILTER_LABLES.IS_NULL, value: FILTER_OPERATORS.IS_NULL, icon: FILTER_ICONS.IS_NULL },
    {
      label: FILTER_LABLES.IS_NOT_NULL,
      value: FILTER_OPERATORS.IS_NOT_NULL,
      icon: FILTER_ICONS.IS_NOT_NULL,
    },
  ];
  numericOperators: FilterOperator[] = [
    { label: FILTER_LABLES.EQUALS, value: FILTER_OPERATORS.EQUALS, icon: FILTER_ICONS.EQUALS },
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
    { label: FILTER_LABLES.BETWEEN, value: FILTER_OPERATORS.BETWEEN, icon: FILTER_ICONS.BETWEEN },
    { label: FILTER_LABLES.IS_NULL, value: FILTER_OPERATORS.IS_NULL, icon: FILTER_ICONS.IS_NULL },
    {
      label: FILTER_LABLES.IS_NOT_NULL,
      value: FILTER_OPERATORS.IS_NOT_NULL,
      icon: FILTER_ICONS.IS_NOT_NULL,
    },
  ];
  booleanOperators: FilterOperator[] = [
    { label: FILTER_LABLES.EQUALS, value: FILTER_OPERATORS.EQUALS, icon: FILTER_ICONS.EQUALS },
    {
      label: FILTER_LABLES.NOT_EQUALS,
      value: FILTER_OPERATORS.NOT_EQUALS,
      icon: FILTER_ICONS.NOT_EQUALS,
    },
    { label: FILTER_LABLES.IS_NULL, value: FILTER_OPERATORS.IS_NULL, icon: FILTER_ICONS.IS_NULL },
    {
      label: FILTER_LABLES.IS_NOT_NULL,
      value: FILTER_OPERATORS.IS_NOT_NULL,
      icon: FILTER_ICONS.IS_NOT_NULL,
    },
  ];

  private valueChangeSubject = new Subject<GridFilter>();
  private destroy$ = new Subject<void>();
  readonly FILTER_OPERATORS = FILTER_OPERATORS;
  betweenError = '';

  constructor(private elementRef: ElementRef) {}

  ngOnInit(): void {
    this.initializeFilter();
    this.operators = this.getOperators(this.column);

    this.valueChangeSubject
      .pipe(debounceTime(500), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((filter) => {
        this.onValueChange(filter);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.valueChangeSubject.complete();
  }

  onInput(value: string): void {
    this.filter.value = value;
    this.valueChangeSubject.next({ ...this.filter });
  }

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

  getOperators(column: GridColumn) {
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
    } else if (operator.value === 'isNull' || operator.value === 'isNotNull') {
      this.filter.value = '';
      this.filter.valueTo = undefined;
    } else {
      this.filter.valueTo = undefined;
    }
    this.operatorChange.emit(this.filter);
  }

  onValueChange(gridFilter: GridFilter): void {
    this.valueChange.emit(gridFilter);
  }

  onBetweenInput(type: 'from' | 'to', value: string): void {
    if (type === 'from') {
      this.filter.value = value;
    } else {
      this.filter.valueTo = value;
    }
    if (!this.isValidBetween()) {
      return;
    }
    this.valueChangeSubject.next({ ...this.filter });
  }

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
      const from = new Date(this.filter.value);
      const to = new Date(this.filter.valueTo);

      if (from > to) {
        this.betweenError = 'To date should be greater than or equal to From date';
        return false;
      }
    }

    return true;
  }

  onReset(): void {
    /*
     * Clear input
     */
    this.betweenError = '';
    this.filter.value = '';
    this.filter.valueTo = undefined;
    this.filter.operator = this.getDefaultOperator(this.column);
    this.selectedOperator = this.filter.operator;
    this.valueChange.emit({ ...this.filter });
    /*
     * Close menu
     */
    this.menuOpen = false;
  }
}
