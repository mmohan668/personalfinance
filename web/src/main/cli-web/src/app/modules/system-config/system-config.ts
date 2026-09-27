import { Component, inject, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { CommonService } from '../shared/service/common-service';
import { GridColumn, ToolbarConfig } from '../shared/types/types';
import { MatDialog } from '@angular/material/dialog';
import { UpdateSystemCurrencyDialog } from './update-system-currency-dialog/update-system-currency-dialog';
import { NotificationService } from '../shared/service/notification-service';
import { SystemConfigService } from '../shared/service/system-config-service';

@Component({
  imports: [DataGrid],
  selector: 'app-system-config',
  styleUrl: './system-config.scss',
  templateUrl: './system-config.html',
})
export class SystemConfig {
  @ViewChild('dataGrid') dataGrid!: DataGrid;

  public _cs = inject(CommonService);
  protected readonly dialog = inject(MatDialog);
  private readonly _ns = inject(NotificationService);
  private readonly _scs = inject(SystemConfigService);

  protected readonly gridName = GRID_NAMES.SYSTEM_CONFIG;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.SYSTEM_CONFIG_EFN;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected toolbarConfig!: ToolbarConfig;

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig();
  }

  hyperlinkAction = (rowData: any, column: GridColumn): void => {
    if (rowData['configName'] === 'Currency') {
      this.dialog
        .open(UpdateSystemCurrencyDialog, {
          width: '35vw',
          data: {
            rowData: rowData,
          },
          disableClose: true,
        })
        .afterClosed()
        .subscribe((message: any) => {
          if (message) {
            this._ns.success(message);
            this.dataGrid?.refreshGrid();
            this._scs.fetchSystemConfig();
          }
        });
    }
  };
}
