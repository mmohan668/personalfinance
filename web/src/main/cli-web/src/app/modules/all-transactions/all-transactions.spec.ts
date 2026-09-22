import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AllTransactions } from './all-transactions';

describe('AllTransactions', () => {
  let component: AllTransactions;
  let fixture: ComponentFixture<AllTransactions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AllTransactions],
    }).compileComponents();

    fixture = TestBed.createComponent(AllTransactions);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
