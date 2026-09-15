import { Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { ApiResponse, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { AdEditReferenceObjectDialog } from './ad-edit-reference-object-dialog/ad-edit-reference-object-dialog';
import { SettingsService } from '../shared/service/settings-service';
import { firstValueFrom } from 'rxjs';

@Component({
  imports: [DataGrid],
  selector: 'app-reference-objects',
  styleUrl: './reference-objects.scss',
  templateUrl: './reference-objects.html',
})
export class ReferenceObjects {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  protected readonly gridName = GRID_NAMES.REFERENCE_OBJECTS_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_OBJECTS_EFN;
  toolbarConfig!: ToolbarConfig;
  private dialog = inject(MatDialog);
  private _cs = inject(CommonService);
  private _ss = inject(SettingsService);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig;
    this.toolbarConfig.addRow = true;
    this.toolbarConfig.deleteRow = true;
  }

  addRow = () => {
    console.log('Add Reference Object');
    this.dialog
      .open(AdEditReferenceObjectDialog, {
        width: '90%',
        data: { mode: 'add' },
      })
      .afterClosed()
      .subscribe((message: any) => {
        console.log(message);
        this.dataGrid.refreshGrid();
      });
  };

  deleteRow = () => {
    console.log('Delete Reference Object');
    if (this.dataGrid.selectedRows.every((row) => row.createdBy === 'SYSTEM')) {
      console.log('Cannot delete System generated reference objects');
      return;
    }
    if (this.dataGrid.selectedRows.some((row) => row.createdBy === 'SYSTEM')) {
      console.log(
        'Some are System generated reference objects, those will be ignored and rest will be deleted',
      );
      return;
    }
    const ids = this.dataGrid.selectedRows
      .filter((item) => item.createdBy !== 'SYSTEM')
      .map((row) => row.id);
    firstValueFrom(this._ss.deleteReferenceObject(ids))
      .then((res: ApiResponse) => {
        console.log(res.message);
        this.dataGrid.refreshGrid();
      })
      .catch((error) => {
        console.log('Error while deleteReferenceObject', error);
      });
  };
}
