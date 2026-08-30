import { Service } from '@angular/core';

@Service()
export class CommonService {
  isNotNull(value: any) {
    return value !== undefined && value !== null && value !== '';
  }
}
