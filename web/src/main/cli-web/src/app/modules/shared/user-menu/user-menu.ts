import { Component, HostListener, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth-service';
import { CommonService } from '../service/common-service';
import { SystemConfigService } from '../service/system-config-service';

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

  profileMenuOpen = false;

  constructor() {}

  ngOnInit(): void {}

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
}
