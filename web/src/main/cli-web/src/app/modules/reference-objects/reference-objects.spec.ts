import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReferenceObjects } from './reference-objects';

describe('ReferenceObjects', () => {
  let component: ReferenceObjects;
  let fixture: ComponentFixture<ReferenceObjects>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReferenceObjects],
    }).compileComponents();

    fixture = TestBed.createComponent(ReferenceObjects);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
