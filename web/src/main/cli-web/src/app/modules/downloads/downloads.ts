import { Component, inject } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, GRID_NAMES } from '../shared/enums';
import { GridColumn, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { GridService } from '../shared/service/grid-service';

@Component({
  imports: [DataGrid],
  selector: 'app-downloads',
  styleUrl: './downloads.scss',
  templateUrl: './downloads.html',
})
export class Downloads {
  public readonly _cs = inject(CommonService);
  protected readonly _gs = inject(GridService);

  protected readonly gridName = GRID_NAMES.DOWNLOADS;
  protected readonly dataKey = DATA_FIELDS.FILE_NAME;
  protected readonly toolbaConfig!: ToolbarConfig;

  constructor() {
    this.toolbaConfig = this._cs.toolbarConfig();
    this.toolbaConfig.refresh = true;
  }

  hasValue = (rowData: any, col: GridColumn) => {
    if (col.field === 'fileName') {
      return this._cs.isNotNull(rowData['filePath']);
    }
    return false;
  };

  downloadFile = (rowData: any, col: GridColumn) => {
    let filePath = '';
    if (col.field === 'fileName') {
      filePath = rowData['filePath'];
    }
    this._gs.downloadFile(filePath).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.getFileName(filePath);
        a.click();
        window.URL.revokeObjectURL(url);
      },
    });
  };

  getFileName(filePath: string): string {
    return filePath.split('\\').pop() || 'download';
  }

  formatSize = (rowData: any): string => {
    const bytes = rowData['size'];
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(2)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  getGroupColumnHeader = () => {
    return 'Grid Name';
  };
}
