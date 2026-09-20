import { TestBed } from '@angular/core/testing';
import { FinancialTransactionsService } from './financial-transactions-service';

describe('FinancialTransactionsService', () => {
  let service: FinancialTransactionsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FinancialTransactionsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
