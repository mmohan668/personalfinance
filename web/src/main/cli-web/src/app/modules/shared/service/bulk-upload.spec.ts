import { TestBed } from '@angular/core/testing';
import { BulkUpload } from './bulk-upload';

describe('BulkUpload', () => {
  let service: BulkUpload;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BulkUpload);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
