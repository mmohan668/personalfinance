import { HttpClient } from '@angular/common/http';
import { Injectable, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { GridColumn, SearchCriteria } from '../types/types';

@Injectable({ providedIn: 'root' })
export class GridService {
  private baseUrl = 'http://localhost:1003/pf-warehouse/grid';
  constructor(private http: HttpClient) {}

  getColumns(): Observable<GridColumn[]> {
    return this.http.get<GridColumn[]>(`${this.baseUrl}/getColumns`);
  }

  getData(request: SearchCriteria): Observable<any[]> {
    return this.http.post<any[]>(`${this.baseUrl}/getData`, request);
  }
}
