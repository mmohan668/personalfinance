import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  ExportRequest,
  GridColumn,
  GridPersonalizationDto,
  GridResult,
  SearchCriteria,
} from '../types/types';
import { GRID_URL_MAP } from '../constants';
import { AppConfigService } from './app-config-service';

@Injectable({ providedIn: 'root' })
export class GridService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  constructor() {}

  loadGridColumns(gridName: string): Observable<GridColumn[]> {
    return this.http.get<GridColumn[]>(
      `${this.config.configValue.apiUrl}/grid/fetchGridColumns?gridName=${gridName}&userId=1`,
    );
  }

  loadGridData(request: SearchCriteria): Observable<GridResult> {
    return this.http.post<GridResult>(
      `${this.config.configValue.apiUrl}${GRID_URL_MAP.get(request.gridName)}`,
      request,
    );
  }

  exportServer(exportRequest: ExportRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/export/exportServer`,
      exportRequest,
    );
  }

  saveGridSetting(gridPersonalizationDto: GridPersonalizationDto): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/grid/saveGridSettings`,
      gridPersonalizationDto,
    );
  }

  resetGridSettings(gridPersonalizationDto: GridPersonalizationDto): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/grid/resetGridSettings`,
      gridPersonalizationDto,
    );
  }

  downloadFile(filePath: any): Observable<Blob> {
    return this.http.get(`${this.config.configValue.apiUrl}/export/download`, {
      params: { filePath: filePath },
      responseType: 'blob',
    });
  }
}
