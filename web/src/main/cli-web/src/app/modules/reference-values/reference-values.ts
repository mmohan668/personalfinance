import { Component, inject, ViewChild } from '@angular/core';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { SelectItem, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { DataGrid } from '../shared/data-grid/data-grid';
import { MatDialog } from '@angular/material/dialog';
import { AdEditReferenceValueDialog } from './ad-edit-reference-value-dialog/ad-edit-reference-value-dialog';
import { SettingsService } from '../shared/service/settings-service';
import { firstValueFrom } from 'rxjs';

@Component({
  imports: [DataGrid],
  selector: 'app-reference-values',
  styleUrl: './reference-values.scss',
  templateUrl: './reference-values.html',
})
export class ReferenceValues {
  @ViewChild('dataGrid') dataGrid!: DataGrid;
  protected readonly gridName = GRID_NAMES.REFERENCE_VALUES_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_VALUES_EFN;
  toolbarConfig!: ToolbarConfig;
  private _cs = inject(CommonService);
  private dialog = inject(MatDialog);
  private _ss = inject(SettingsService);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig;
    this.toolbarConfig.addRow = true;
  }

  addRow = async () => {
    firstValueFrom(this._ss.fetchCategoryTypes())
      .then((res: SelectItem[]) => {
        const categoryTypes: SelectItem[] = res;
        this.dialog
          .open(AdEditReferenceValueDialog, {
            width: '70vw',
            maxWidth: '70vw',
            data: {
              mode: 'Add',
              categoryTypes: categoryTypes,
            },
            disableClose: true,
          })
          .afterClosed()
          .subscribe(() => {
            this.dataGrid?.refreshGrid();
          });
      })
      .catch((error) => {
        console.log('Error while fetchCategoryTypes', error);
      });
  };
}
