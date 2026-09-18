import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddEditCategoryDialog } from './add-edit-category-dialog';

describe('AddEditCategoryDialog', () => {
  let component: AddEditCategoryDialog;
  let fixture: ComponentFixture<AddEditCategoryDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEditCategoryDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AddEditCategoryDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
