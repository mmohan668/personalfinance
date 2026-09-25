import { Injectable, signal } from '@angular/core';
import { GridColumn } from '../types/types';

@Injectable({
  providedIn: 'root',
})
export class GridStateService {
  private readonly _columns = signal<Record<string, GridColumn[]>>({});

  getColumns(gridName: string): GridColumn[] | null {
    return this._columns()[gridName] ?? null;
  }

  setColumns(gridName: string, columns: GridColumn[]): void {
    this._columns.update((state) => ({
      ...state,
      [gridName]: columns,
    }));
  }

  clearColumns(gridName: string): void {
    this._columns.update((state) => {
      const next = { ...state };
      delete next[gridName];

      return next;
    });
  }
}
