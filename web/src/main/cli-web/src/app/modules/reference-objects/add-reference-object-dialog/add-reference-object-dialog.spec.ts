import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddReferenceObjectDialog } from './add-reference-object-dialog';

describe('AddReferenceObjectDialog', () => {
  let component: AddReferenceObjectDialog;
  let fixture: ComponentFixture<AddReferenceObjectDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddReferenceObjectDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AddReferenceObjectDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
