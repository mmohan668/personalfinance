import { HttpClient } from '@angular/common/http';
import { Injectable, Service } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  GridColumn,
  GridPersonalizationDto,
  GridResult,
  SearchCriteria,
} from '../types/types';
import { GRID_URL_MAP } from '../constants';

@Injectable({ providedIn: 'root' })
export class GridService {
  private baseUrl = 'http://localhost:1003';
  constructor(private http: HttpClient) {}

  loadGridColumns(gridName: string): Observable<GridColumn[]> {
    return this.http.get<GridColumn[]>(
      `${this.baseUrl}/grid/fetchGridColumns?gridName=${gridName}&userId=1`,
    );
  }

  loadGridData(request: SearchCriteria): Observable<GridResult> {
    return this.http.post<GridResult>(
      `${this.baseUrl}${GRID_URL_MAP.get(request.gridName)}`,
      request,
    );
  }

  saveGridSetting(gridPersonalizationDto: GridPersonalizationDto): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.baseUrl}/grid/saveGridSettings`,
      gridPersonalizationDto,
    );
  }
}
