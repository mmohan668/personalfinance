import { AfterViewInit, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService, LoginResponse } from '../shared/service/auth-service';
import { MatDialog } from '@angular/material/dialog';
import { ForgotPasswordDialog } from '../users/forgot-password-dialog/forgot-password-dialog';
import { CommonImportsModule } from '../shared/common-imports/common-imports-module';
import { ChangePasswordDialog } from '../users/change-password-dialog/change-password-dialog';
import { CommonService } from '../shared/service/common-service';
import { AppConfigService } from '../shared/service/app-config-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonImportsModule, RouterLink],
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login implements AfterViewInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private _cs = inject(CommonService);
  private _acs = inject(AppConfigService);

  username = '';
  password = '';
  errorMessage = '';
  isLoading = signal<boolean>(false);
  showPassword = false;

  private googleClientId: string | null = null;
  private googleScriptLoaded = false;

  ngAfterViewInit(): void {
    const googleScript = document.getElementById('google-identity-script');

    googleScript?.addEventListener('load', () => {
      this.googleScriptLoaded = true;
      this.initializeGoogleSignIn();
    });
    this.authService.getGoogleClientId().subscribe({
      next: (response) => {
        this.googleClientId = response.clientId;

        if (window.google) {
          this.googleScriptLoaded = true;
        }

        this.initializeGoogleSignIn();
      },
      error: (error) => {
        console.error('Unable to load Google Client ID:', error);
      },
    });
  }

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

          this.completeLogin(response);
        },
        error: (error) => {
          this.isLoading.set(false);
          this.errorMessage = error?.error?.message ?? 'Login failed. Please try again.';
        },
      });
  }

  private initializeGoogleSignIn(): void {
    if (!this.googleClientId || !this.googleScriptLoaded || !window.google) {
      return;
    }

    window.google.accounts.id.initialize({
      client_id: this.googleClientId,
      callback: (response: GoogleCredentialResponse) => {
        this.onGoogleLogin(response.credential);
      },
    });

    const buttonContainer = document.getElementById('google-signin-button');

    if (buttonContainer) {
      buttonContainer.innerHTML = '';

      window.google.accounts.id.renderButton(buttonContainer, {
        theme: 'outline',
        size: 'large',
        text: 'continue_with',
        shape: 'rectangular',
        width: 300,
      });
    }
  }

  private onGoogleLogin(credential: string): void {
    if (this.isLoading()) {
      return;
    }

    this.errorMessage = '';
    this.isLoading.set(true);

    this.authService.authenticateWithGoogle(credential).subscribe({
      next: (response) => {
        if (!response.accessToken) {
          this.errorMessage = 'No access token was returned by the server.';
          this.isLoading.set(false);
          return;
        }

        this.completeLogin(response);
      },
      error: (error) => {
        this.isLoading.set(false);
        this.errorMessage = error?.error?.message ?? 'Google sign-in failed. Please try again.';
      },
    });
  }

  private completeLogin(response: LoginResponse): void {
    this._acs.loadFromWebService().subscribe({
      next: () => {
        this.authService.storeToken(response.accessToken);
        this.authService.storeUserInfo(response.userProfile);
        this.authService.storePermissions(response.permissions);
        this.finishLogin(response);
      },
      error: (error) => {
        console.error('Unable to load web application configuration:', error);
        this.isLoading.set(false);
        this.errorMessage = 'Unable to load application settings. Please try signing in again.';
      },
    });
  }

  private finishLogin(response: LoginResponse): void {
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
      return;
    }
    this._cs.identifyDevice();
    void this.router.navigate(['/']);
  }
}
