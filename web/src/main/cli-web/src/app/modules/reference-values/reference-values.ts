import { Component, inject } from '@angular/core';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { DataGrid } from '../shared/data-grid/data-grid';

@Component({
  imports: [DataGrid],
  selector: 'app-reference-values',
  styleUrl: './reference-values.scss',
  templateUrl: './reference-values.html',
})
export class ReferenceValues {
  protected readonly gridName = GRID_NAMES.REFERENCE_VALUES_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_VALUES_EFN;
  toolbarConfig!: ToolbarConfig;
  private _cs = inject(CommonService);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig;
  }
}
