import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../shared/service/auth-service';
import { MatDialog } from '@angular/material/dialog';
import { ForgotPasswordDialog } from '../users/forgot-password-dialog/forgot-password-dialog';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { ChangePasswordDialog } from '../users/change-password-dialog/change-password-dialog';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonImportsModule, RouterLink],
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private authService = inject(AuthService);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  username = '';
  password = '';
  errorMessage = '';
  isLoading = signal<boolean>(false);
  showPassword = false;

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  openForgotPasswordDialog(event: Event): void {
    event.preventDefault();

    this.dialog.open(ForgotPasswordDialog, {
      width: '520px',
      maxWidth: 'calc(100vw - 32px)',
      maxHeight: 'calc(100dvh - 32px)',
      autoFocus: false,
      panelClass: 'forgot-password-panel',
    });
  }

  onLogin(): void {
    this.showPassword = false;

    if (this.isLoading()) {
      return;
    }

    this.errorMessage = '';
    this.isLoading.set(true);

    this.authService
      .authenticate({
        username: this.username.trim(),
        password: this.password,
      })
      .subscribe({
        next: (response) => {
          if (!response.accessToken) {
            this.errorMessage = 'No access token was returned by the server.';
            this.isLoading.set(false);
            return;
          }

          this.authService.storeToken(response.accessToken);
          this.authService.storeUserInfo(response.userProfile);
          this.authService.storePermissions(response.permissions);
          this.isLoading.set(false);

          if (response.mustChangePassword) {
            const dialogRef = this.dialog.open(ChangePasswordDialog, {
              width: '520px',
              maxWidth: 'calc(100vw - 32px)',
              maxHeight: 'calc(100dvh - 32px)',
              autoFocus: false,
              panelClass: 'change-password-panel',
            });
            dialogRef.componentInstance.forcePasswordChange = true;
          } else {
            if (response.mustChangePassword) {
              const dialogRef = this.dialog.open(ChangePasswordDialog, {
                width: '520px',
                maxWidth: 'calc(100vw - 32px)',
                maxHeight: 'calc(100dvh - 32px)',
                autoFocus: false,
                panelClass: 'change-password-panel',
              });

              dialogRef.afterClosed().subscribe((result) => {
                if (result) {
                  void this.router.navigate(['/']);
                } else {
                  // The password has not been changed.
                  // Keep the user on the login page.
                  this.authService.logout();
                }
              });
            } else {
              void this.router.navigate(['/']);
            }
          }
        },
        error: (error) => {
          this.isLoading.set(false);
          this.errorMessage = error?.error?.message ?? 'Login failed. Please try again.';
        },
      });
  }
}
