import { Component, HostListener, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../service/user-service';

export interface UserProfile {
  name: string;
  email: string;
  imageUrl?: string;
}

@Component({
  selector: 'app-user-menu',
  standalone: true,
  templateUrl: './user-menu.html',
  styleUrls: ['./user-menu.scss'],
})
export class UserMenu implements OnInit {
  profileMenuOpen = false;
  user: UserProfile | null = null;

  constructor(
    private _us: UserService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Replace with the method provided by your AuthService.
    this.user = this._us.getCurrentUser();
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

    // Use your existing authentication service's logout method.
    this._us.logout();
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
