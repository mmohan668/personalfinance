import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { vi } from 'vitest';
import { CommonService } from './common-service';

describe('CommonService', () => {
  let service: CommonService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CommonService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('closes open dialogs when clearing the user session', () => {
    const dialog = TestBed.inject(MatDialog);
    const closeAll = vi.spyOn(dialog, 'closeAll');

    service.clearUserSession();

    expect(closeAll).toHaveBeenCalled();
  });
});
