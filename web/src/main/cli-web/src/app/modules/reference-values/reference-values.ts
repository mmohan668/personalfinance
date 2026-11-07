import { Component, inject, signal, ViewChild } from '@angular/core';
import { DATA_FIELDS, ENTITIES, GRID_EXPORT_FILE_NAMES, GRID_NAMES, MODES } from '../shared/enums';
import { GridColumn, SuggestionRequest, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { DataGrid } from '../shared/data-grid/data-grid';
import { MatDialog } from '@angular/material/dialog';
import { AdEditReferenceValueDialog } from './ad-edit-reference-value-dialog/ad-edit-reference-value-dialog';
import { SettingsService } from '../shared/service/settings-service';
import { debounceTime, firstValueFrom } from 'rxjs';
import { NotificationService } from '../shared/service/notification-service';
import { ConfirmationDialog } from '../shared/confirmation-dialog/confirmation-dialog';
import { MessageService } from '../shared/service/message-service';
import { FormControl, FormGroup } from '@angular/forms';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { AppConfigService } from '../shared/service/app-config-service';
import { SuggestionService } from '../shared/service/suggestion-service';

@Component({
  imports: [DataGrid, CommonImportsModule],
  selector: 'app-reference-values',
  styleUrl: './reference-values.scss',
  templateUrl: './reference-values.html',
})
export class ReferenceValues {
  @ViewChild('dataGrid') private dataGrid!: DataGrid;

  public readonly _cs = inject(CommonService);
  private readonly dialog = inject(MatDialog);
  private readonly _ss = inject(SettingsService);
  private readonly _ns = inject(NotificationService);
  protected readonly _ms = inject(MessageService);
  private readonly _acs = inject(AppConfigService);
  private readonly _sgs = inject(SuggestionService);

  protected readonly gridName = GRID_NAMES.REFERENCE_VALUES_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_VALUES_EFN;
  protected readonly toolbarConfig!: ToolbarConfig;
  protected searchForm!: FormGroup;

  refObjNames = signal<string[]>([]);
  referenceCodes = signal<string[]>([]);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig(true, 'Settings', 'Reference Values');
    this.createSearchForm();
    this.fetchSuggestions();
  }

  createSearchForm(): void {
    this.searchForm = new FormGroup({
      refObjName: new FormControl(''),
      referenceCode: new FormControl(''),
      active: new FormControl(''),
    });
  }

  fetchSuggestions() {
    this.searchForm.controls['refObjName'].valueChanges
      .pipe(debounceTime(this._acs.configValue.debounceTime))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.REFERENCE_OBJECT,
            field: DATA_FIELDS.REF_OBJ_NAME,
            value: val,
          };
          firstValueFrom(this._sgs.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.refObjNames.set(resp);
          });
        }
      });

    this.searchForm.controls['referenceCode'].valueChanges
      .pipe(debounceTime(this._acs.configValue.debounceTime))
      .subscribe((val) => {
        if (this._cs.isNotNull(val)) {
          const suggestionRequest: SuggestionRequest = {
            entity: ENTITIES.REFERENCE_VALUE,
            field: DATA_FIELDS.REFERENCE_CODE,
            value: val,
          };
          firstValueFrom(this._sgs.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.referenceCodes.set(resp);
          });
        }
      });
  }

  calculateCellValue = (rowData: any, col: GridColumn): any => {
    if (col.field === 'active') {
      return rowData[col.field] ? 'Active' : 'Inactive';
    }
    return rowData[col.field];
  };

  addRow = (): void => {
    this.dialog
      .open(AdEditReferenceValueDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.ADD,
        },
      })
      .afterClosed()
      .subscribe((message) => {
        if (message) {
          this._ns.success(message);
          this.dataGrid?.refreshGrid();
        }
      });
  };

  deleteRow = (): void => {
    if (this.dataGrid?.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.delete.noSelection'));
      return;
    }
    const title = this._ms.get('common.delete.title');
    const message = this._ms.get('referenceValue.delete.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: {
          title: title,
          message: message,
          isNotification: false,
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid.selectedRows.map((row) => row.id);
          firstValueFrom(this._ss.deleteReferenceValue(ids)).then((response) => {
            if (response.success) {
              this._ns.success(response.message);
              this.dataGrid.refreshGrid();
            } else {
              this._ns.error(response.message);
            }
          });
        }
      });
  };

  editRow = (): void => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.edit.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.length > 1) {
      this._ns.error(this._ms.get('common.edit.singleSelection'));
      return;
    }
    this.dialog
      .open(AdEditReferenceValueDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.EDIT,
          selectedRow: this.dataGrid.selectedRows[0],
        },
      })
      .afterClosed()
      .subscribe((message) => {
        if (message) {
          this._ns.success(message);
          this.dataGrid.refreshGrid();
        }
      });
  };

  copyRow = (): void => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.copy.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.length > 1) {
      this._ns.error(this._ms.get('common.copy.singleSelection'));
      return;
    }
    this.dialog
      .open(AdEditReferenceValueDialog, {
        width: '70vw',
        maxWidth: '70vw',
        data: {
          mode: MODES.COPY,
          selectedRow: this.dataGrid.selectedRows[0],
        },
      })
      .afterClosed()
      .subscribe((message) => {
        if (message) {
          this._ns.success(message);
          this.dataGrid.refreshGrid();
        }
      });
  };

  activate = (): void => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.activate.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.every((row) => row.active === true)) {
      this._ns.error(this._ms.get('referenceValue.activate.alreadyActive'));
      return;
    }
    const someActive = this.dataGrid.selectedRows.some((row) => row.active === true);
    const title = this._ms.get('common.activate.title');
    const message = someActive
      ? this._ms.get('referenceValue.activate.mixedSelection')
      : this._ms.get('referenceValue.activate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: {
          title: title,
          message: message,
          isNotification: false,
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid.selectedRows.map((row) => row.id);
          firstValueFrom(this._ss.activateReferenceValue(ids)).then((response) => {
            if (response.success) {
              this._ns.success(response.message);
              this.dataGrid.refreshGrid();
            } else {
              this._ns.error(response.message);
            }
          });
        }
      });
  };

  inactivate = (): void => {
    if (this.dataGrid.selectedRows.length === 0) {
      this._ns.error(this._ms.get('common.inactivate.noSelection'));
      return;
    }
    if (this.dataGrid.selectedRows.every((row) => row.active === false)) {
      this._ns.error(this._ms.get('referenceValue.inactivate.alreadyInactive'));
      return;
    }
    const someInactive = this.dataGrid.selectedRows.some((row) => row.active === false);
    const title = this._ms.get('common.inactivate.title');
    const message = someInactive
      ? this._ms.get('referenceValue.inactivate.mixedSelection')
      : this._ms.get('referenceValue.inactivate.confirmation');
    this.dialog
      .open(ConfirmationDialog, {
        width: 'auto',
        data: {
          title: title,
          message: message,
          isNotification: false,
        },
      })
      .afterClosed()
      .subscribe((value: boolean) => {
        if (value) {
          const ids = this.dataGrid.selectedRows.map((row) => row.id);
          firstValueFrom(this._ss.inactivateReferenceValue(ids)).then((response) => {
            if (response.success) {
              this._ns.success(response.message);
              this.dataGrid.refreshGrid();
            } else {
              this._ns.error(response.message);
            }
          });
        }
      });
  };

  onSearch(): void {
    this.dataGrid.clearFilters();
    this.dataGrid.refreshGrid(this._cs.prepareSearchCriteria(this.searchForm.value));
  }

  resetSearchForm(): void {
    this.searchForm.reset({
      active: '',
    });
    this.dataGrid.resetAllFilters();
  }
}
