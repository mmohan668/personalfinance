import { Service, signal } from '@angular/core';

@Service()
export class LoadingService {
  private activeRequests = 0;

  loading = signal(false);

  show(): void {
    this.activeRequests++;
    this.loading.set(true);
  }

  hide(): void {
    this.activeRequests = Math.max(0, this.activeRequests - 1);

    if (this.activeRequests === 0) {
      this.loading.set(false);
    }
  }
}
