import { Component, Input } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { GridColumn, ToolbarConfig } from '../types/types';
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
  public columnSearch: string = '';
  @Input()
  public exportExcel!: (value: string) => void;
  @Input()
  public allColumnsSelected!: () => boolean;
  @Input()
  public someColumnsSelected!: () => boolean;
  @Input()
  public toggleAllColumns!: (checked: boolean) => void;
  @Input()
  public getColumns!: (searchInput: string) => GridColumn[];
  @Input()
  public toggleColumn!: (col: GridColumn, checked: boolean) => void;
  @Input()
  public addRow!: () => void;
  @Input()
  public copyRow!: () => void;
  @Input()
  public editRow!: () => void;
  @Input()
  public deleteRow!: () => void;
  @Input()
  public activate!: () => void;
  @Input()
  public inactivate!: () => void;
  @Input()
  public saveGridSetting!: () => void;
  @Input()
  public resetGridSettings!: () => void;
  @Input()
  public toolbarConfig!: ToolbarConfig;
}
