import { Component, inject } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { AdEditReferenceObjectDialog } from './ad-edit-reference-object-dialog/ad-edit-reference-object-dialog';

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
  private dialog = inject(MatDialog);

  constructor(public commonService: CommonService) {
    this.toolbarConfig = commonService.toolbarConfig;
    this.toolbarConfig.addRow = true;
  }

  addRow = () => {
    console.log('Add Reference Object');
    this.dialog.open(AdEditReferenceObjectDialog, {
      width: '90%',
      data: { mode: 'add' },
    });
  };
}
