import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

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
}
