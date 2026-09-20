import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddEditExpenseDialog } from './add-edit-expense-dialog';

describe('AddEditExpenseDialog', () => {
  let component: AddEditExpenseDialog;
  let fixture: ComponentFixture<AddEditExpenseDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEditExpenseDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AddEditExpenseDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
