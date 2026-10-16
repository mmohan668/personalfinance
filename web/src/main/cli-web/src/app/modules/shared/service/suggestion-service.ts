import { inject, Service } from '@angular/core';
import { AppConfigService } from './app-config-service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SuggestionRequest } from '../types/types';

@Service()
export class SuggestionService {
  private config = inject(AppConfigService);
  private http = inject(HttpClient);

  fetchSuggestion(suggestionRequest: SuggestionRequest): Observable<any> {
    suggestionRequest.limit = this.config.configValue.maxSuggestionRows;
    return this.http.post<any>(
      `${this.config.configValue.apiUrl}/suggestion/fetchSuggestion`,
      suggestionRequest,
    );
  }
}
