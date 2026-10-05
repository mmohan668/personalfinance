import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddEditReferenceObjectDialog } from './add-edit-reference-object-dialog';

describe('AddEditReferenceObjectDialog', () => {
  let component: AddEditReferenceObjectDialog;
  let fixture: ComponentFixture<AddEditReferenceObjectDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEditReferenceObjectDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AddEditReferenceObjectDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
