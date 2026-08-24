import { Component, EventEmitter, Input, Output } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';

export interface FilterOperator {
  value: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-column-filter',
  standalone: true,
  imports: [InputTextModule],
  templateUrl: './column-filter.html',
  styleUrl: './column-filter.scss',
})
export class ColumnFilterComponent {
  @Input()
  value = '';

  @Input()
  placeholder = '';

  @Input()
  type: 'text' | 'number' = 'text';

  @Input()
  operators: FilterOperator[] = [];

  @Input()
  selectedOperator = '';

  /**
   * Operator to use after Reset.
   *
   * If not supplied, the first operator is used.
   */
  @Input()
  defaultOperator = '';

  @Output()
  valueChange = new EventEmitter<string>();

  @Output()
  operatorChange = new EventEmitter<string>();

  /**
   * Emitted when Reset is clicked.
   */
  @Output()
  reset = new EventEmitter<void>();

  menuOpen = false;

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

    this.operatorChange.emit(operator.value);
  }

  onValueChange(value: string): void {
    this.value = value;

    this.valueChange.emit(value);
  }

  onReset(): void {
    /*
     * Clear input
     */
    this.value = '';

    this.valueChange.emit('');

    /*
     * Restore default operator
     */
    const operator = this.defaultOperator || this.operators[0]?.value || '';

    if (operator) {
      this.selectedOperator = operator;

      this.operatorChange.emit(operator);
    }

    /*
     * Close menu
     */
    this.menuOpen = false;

    /*
     * Tell parent to clear its table filter.
     */
    this.reset.emit();
  }
}
