import { Component, Input } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { GridColumn } from '../types/types';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';

@Component({
  imports: [
    MatIconModule,
    MatMenuModule,
    MatFormFieldModule,
    MatCheckboxModule,
    FormsModule,
    MatInputModule,
  ],
  selector: 'app-tool-bar',
  styleUrl: './tool-bar.scss',
  templateUrl: './tool-bar.html',
  standalone: true,
})
export class ToolBar {
  @Input()
  columnSearch: string = '';
  @Input()
  exportExcel!: (value: string) => void;
  @Input()
  allColumnsSelected!: () => boolean;
  @Input()
  someColumnsSelected!: () => boolean;
  @Input()
  toggleAllColumns!: (checked: boolean) => void;
  @Input()
  getColumns!: (searchInput: string) => GridColumn[];
  @Input()
  toggleColumn!: (col: GridColumn, checked: boolean) => void;
}
