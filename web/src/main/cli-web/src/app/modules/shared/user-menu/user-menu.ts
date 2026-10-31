import { Component, HostListener, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth-service';
import { CommonService } from '../service/common-service';
import { SystemConfigService } from '../service/system-config-service';
import { MatDialog } from '@angular/material/dialog';
import { ProfilePictureDialog } from '../../users/profile-picture-dialog/profile-picture-dialog';
import { UserService } from '../service/user-service';
import { NotificationService } from '../service/notification-service';

@Component({
  selector: 'app-user-menu',
  standalone: true,
  templateUrl: './user-menu.html',
  styleUrls: ['./user-menu.scss'],
})
export class UserMenu implements OnInit {
  private _as = inject(AuthService);
  public _scs = inject(SystemConfigService);
  private router = inject(Router);
  public _cs = inject(CommonService);
  public _us = inject(UserService);
  public _ns = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  profileMenuOpen = false;
  profilePictureUrl = signal<any>('');

  constructor() {}

  ngOnInit(): void {
    this.loadProfilePicture();
  }

  openProfilePictureDialog(event: MouseEvent): void {
    event.stopPropagation();
    this.profileMenuOpen = false;
    this.dialog
      .open(ProfilePictureDialog, {
        width: '450px',
        maxWidth: '95vw',
        disableClose: true,
        autoFocus: false,
        panelClass: 'profile-picture-dialog-panel',
      })
      .afterClosed()
      .subscribe((success) => {
        if (success) {
          this._ns.success('Profile picture uploaded successfully.');
          this.loadProfilePicture();
        }
      });
  }

  displayCurrencySymbol(): string {
    const currencyCode = this._scs.currency();
    const locale = this._scs.locale() ?? 'en-US';

    return (
      new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: currencyCode,
        currencyDisplay: 'narrowSymbol',
      })
        .formatToParts(0)
        .find((part) => part.type === 'currency')?.value ?? currencyCode
    );
  }

  toggleProfileMenu(): void {
    this.profileMenuOpen = !this.profileMenuOpen;
  }

  changePassword(): void {
    this.closeProfileMenu();
    this.router.navigate(['/change-password']);
  }

  openAbout(): void {
    this.closeProfileMenu();
    this.router.navigate(['/about']);
  }

  openHelp(): void {
    this.closeProfileMenu();
    this.router.navigate(['/help']);
  }

  logout(): void {
    this.closeProfileMenu();
    this._as.logout();
  }

  private closeProfileMenu(): void {
    this.profileMenuOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;

    if (!target.closest('.profile-container')) {
      this.closeProfileMenu();
    }
  }

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    this.closeProfileMenu();
  }

  loadProfilePicture() {
    this._us.loadProfilePicture(this._cs.getUserProfile.username).subscribe({
      next: (blob: Blob) => {
        if (this.profilePictureUrl()) {
          URL.revokeObjectURL(this.profilePictureUrl());
        }

        this.profilePictureUrl.set(URL.createObjectURL(blob));
      },
      error: (error) => {
        console.error('Failed to load profile picture:', error);
        this.profilePictureUrl.set('');
      },
    });
  }
}
