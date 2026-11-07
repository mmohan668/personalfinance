import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UpdateSystemCurrencyDialog } from './update-system-currency-dialog';

describe('UpdateSystemCurrencyDialog', () => {
  let component: UpdateSystemCurrencyDialog;
  let fixture: ComponentFixture<UpdateSystemCurrencyDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateSystemCurrencyDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateSystemCurrencyDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
