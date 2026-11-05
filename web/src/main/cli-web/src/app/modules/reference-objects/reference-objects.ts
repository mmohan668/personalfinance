import { Component, inject, signal, ViewChild } from '@angular/core';
import { DataGrid } from '../shared/data-grid/data-grid';
import { DATA_FIELDS, ENTITIES, GRID_EXPORT_FILE_NAMES, GRID_NAMES } from '../shared/enums';
import { SuggestionRequest, ToolbarConfig } from '../shared/types/types';
import { CommonService } from '../shared/service/common-service';
import { MatDialog } from '@angular/material/dialog';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { FormControl, FormGroup } from '@angular/forms';
import { AppConfigService } from '../shared/service/app-config-service';
import { SuggestionService } from '../shared/service/suggestion-service';
import { debounceTime, firstValueFrom } from 'rxjs';

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
  private readonly _ss = inject(SuggestionService);
  private readonly _acs = inject(AppConfigService);

  protected readonly gridName = GRID_NAMES.REFERENCE_OBJECTS_GRID;
  protected readonly dataKey = DATA_FIELDS.ID;
  protected readonly gridExportFileName = GRID_EXPORT_FILE_NAMES.REFERENCE_OBJECTS_EFN;
  protected readonly toolbarConfig!: ToolbarConfig;
  protected searchForm!: FormGroup;
  protected readonly fieldCount = 2;

  refObjNames = signal<string[]>([]);

  constructor() {
    this.toolbarConfig = this._cs.toolbarConfig();
    this.createSearchForm();
    this.fetchSuggestions();
  }

  createSearchForm = () => {
    this.searchForm = new FormGroup({
      refObjName: new FormControl(''),
    });
  };

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
          firstValueFrom(this._ss.fetchSuggestion(suggestionRequest)).then((resp) => {
            this.refObjNames.set(resp);
          });
        }
      });
  }

  onSearch() {
    this.dataGrid.clearFilters();
    this.dataGrid.refreshGrid(this._cs.prepareSearchCriteria(this.searchForm.value));
  }

  resetSearchForm() {
    this.searchForm.reset();
    this.dataGrid.resetAllFilters();
  }
}
