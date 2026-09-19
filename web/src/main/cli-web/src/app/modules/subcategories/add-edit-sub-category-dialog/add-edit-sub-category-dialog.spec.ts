import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddEditSubCategoryDialog } from './add-edit-sub-category-dialog';

describe('AddEditSubCategoryDialog', () => {
  let component: AddEditSubCategoryDialog;
  let fixture: ComponentFixture<AddEditSubCategoryDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEditSubCategoryDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AddEditSubCategoryDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
