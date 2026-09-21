import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddCopyEditFinancialTransactionDialog } from './add-copy-edit-financialtransaction-dialog';

describe('AddCopyEditFinancialTransactionDialog', () => {
  let component: AddCopyEditFinancialTransactionDialog;
  let fixture: ComponentFixture<AddCopyEditFinancialTransactionDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCopyEditFinancialTransactionDialog],l̥
    }).compileComponents();

    fixture = TestBed.createComponent(AddCopyEditFinancialTransactionDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });
l̥
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
