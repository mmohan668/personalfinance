import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '../../shared/service/auth-service';
import { NotificationService } from '../../shared/service/notification-service';
import { ApiResponse, CaptchaResponse, ForgotPasswordRequest } from '../../shared/types/types';

@Component({
  selector: 'app-forgot-password-dialog',
  standalone: true,
  imports: [FormsModule],
  styleUrl: './forgot-password-dialog.scss',
  templateUrl: './forgot-password-dialog.html',
})
export class ForgotPasswordDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<ForgotPasswordDialog>);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  username = '';
  displayName = '';
  email = '';

  captchaId = signal('');
  captchaImage = signal('');
  captchaAnswer = signal('');

  loadingCaptcha = signal(false);
  submitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  ngOnInit(): void {
    this.loadCaptcha();
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.submitting()) {
      return;
    }

    this.errorMessage.set('');
    this.successMessage.set('');

    const request: ForgotPasswordRequest = {
      username: this.username.trim(),
      displayName: this.displayName.trim(),
      email: this.email.trim(),
      captchaId: this.captchaId(),
      captchaAnswer: this.captchaAnswer().trim(),
    };

    this.submitting.set(true);

    this.authService.forgotPassword(request).subscribe({
      next: (response: ApiResponse) => {
        this.submitting.set(false);
        this.successMessage.set(
          response.message ||
            'If your account details are valid, a temporary password will be sent to your registered email.',
        );
      },
      error: (error) => {
        this.submitting.set(false);

        this.errorMessage.set(
          error?.error?.message ||
            'Unable to process your request. Please verify your details and try again.',
        );

        // CAPTCHA may have expired or been consumed. Load a fresh one.
        this.loadCaptcha();
      },
    });
  }

  loadCaptcha(): void {
    this.loadingCaptcha.set(true);
    this.captchaAnswer.set('');
    this.errorMessage.set('');

    this.authService.getCaptcha().subscribe({
      next: (response: CaptchaResponse) => {
        this.captchaId.set(response.captchaId);
        this.captchaImage.set(response.captchaImage);
        this.loadingCaptcha.set(false);
      },
      error: () => {
        this.loadingCaptcha.set(false);
        this.errorMessage.set('Unable to load CAPTCHA. Please try again.');
      },
    });
  }
}
