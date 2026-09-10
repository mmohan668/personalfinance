import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdEditReferenceObjectDialog } from './ad-edit-reference-object-dialog';

describe('AdEditReferenceObjectDialog', () => {
  let component: AdEditReferenceObjectDialog;
  let fixture: ComponentFixture<AdEditReferenceObjectDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdEditReferenceObjectDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AdEditReferenceObjectDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
