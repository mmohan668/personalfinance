import { Component, HostListener, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../service/user-service';
import { AuthService } from '../service/auth-service';
import { CommonService } from '../service/common-service';

@Component({
  selector: 'app-user-menu',
  standalone: true,
  templateUrl: './user-menu.html',
  styleUrls: ['./user-menu.scss'],
})
export class UserMenu implements OnInit {
  private _as = inject(AuthService);
  private _us = inject(UserService);
  private router = inject(Router);
  public _cs = inject(CommonService);

  profileMenuOpen = false;

  constructor() {}

  ngOnInit(): void {}

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

    // Use your existing authentication service's logout method.
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
