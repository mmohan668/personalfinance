import { Component } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { Console } from 'console';

@Component({
  imports: [DataGrid],
  selector: 'app-reference-objects',
  styleUrl: './reference-objects.scss',
  templateUrl: './reference-objects.html',
})
export class ReferenceObjects {
  protected readonly gridName = GRID_NAMES.REFERENCE_OBJECTS_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_OBJECTS_EFN;
  toolbarConfig!: ToolbarConfig;

  constructor(public commonService: CommonService) {
    this.toolbarConfig = commonService.toolbarConfig;
    this.toolbarConfig.addRow = true;
  }

  addRow = () => {
    console.log('Add Reference Object');
  };
}
