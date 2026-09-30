import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../types/types';

@Service()
export class BulkUploadService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  downloadTemplate(selectedUploadType: string): Observable<HttpResponse<Blob>> {
    return this.http.get(
      `${this.config.configValue.apiUrl}/bulkupload/downloadTemplate?templateName=${encodeURIComponent(selectedUploadType)}`,
      {
        responseType: 'blob',
        observe: 'response',
      },
    );
  }

  uploadTemplate(formData: FormData): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.config.configValue.apiUrl}/bulkupload/uploadTemplate`,
      formData,
    );
  }

  downloadFile(filePath: string): Observable<Blob> {
    return this.http.get(`${this.config.configValue.apiUrl}/bulkupload/downloadFile`, {
      params: {
        filePath: filePath,
      },
      responseType: 'blob',
    });
  }
}
