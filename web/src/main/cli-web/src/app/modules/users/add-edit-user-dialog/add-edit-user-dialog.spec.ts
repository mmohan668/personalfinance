import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddEditUserDialog } from './add-edit-user-dialog';

describe('AddEditUserDialog', () => {
  let component: AddEditUserDialog;
  let fixture: ComponentFixture<AddEditUserDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEditUserDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AddEditUserDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
