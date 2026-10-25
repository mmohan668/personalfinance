import { Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  imports: [DataGrid, CommonImportsModule],
  selector: 'app-reference-objects',
  styleUrl: './reference-objects.scss',
  templateUrl: './reference-objects.html',
})
export class ReferenceObjects {
  @ViewChild('dataGrid') dataGrid!: DataGrid;

  public readonly _cs = inject(CommonService);
  protected readonly dialog = inject(MatDialog);

  protected readonly gridName = GRID_NAMES.REFERENCE_OBJECTS_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_OBJECTS_EFN;
  protected readonly toolbarConfig!: ToolbarConfig;
  protected searchForm!: FormGroup;
  protected readonly fieldCount = 2;

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig();
    this.createSearchForm();
  }

  createSearchForm = () => {
    this.searchForm = new FormGroup({
      refObjName: new FormControl(''),
    });
  };

  onSearch() {
    this.dataGrid.clearFilters();
    this.dataGrid.refreshGrid(this._cs.prepareSearchCriteria(this.searchForm.value));
  }

  resetSearchForm() {
    this.searchForm.reset();
    this.dataGrid.resetAllFilters();
  }
}
