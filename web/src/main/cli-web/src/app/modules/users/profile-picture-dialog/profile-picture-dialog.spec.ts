import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfilePictureDialog } from './profile-picture-dialog';

describe('ProfilePictureDialog', () => {
  let component: ProfilePictureDialog;
  let fixture: ComponentFixture<ProfilePictureDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfilePictureDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfilePictureDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
