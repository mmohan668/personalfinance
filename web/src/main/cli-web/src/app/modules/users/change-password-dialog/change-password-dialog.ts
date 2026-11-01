import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { UserService } from '../../shared/service/user-service';
import { ApiResponse } from '../../shared/types/types';
import { CommonService } from '../../shared/service/common-service';

@Component({
  selector: 'app-change-password-dialog',
  standalone: true,
  imports: [FormsModule],
  styleUrl: './change-password-dialog.scss',
  templateUrl: './change-password-dialog.html',
})
export class ChangePasswordDialog {
  private readonly dialogRef = inject(MatDialogRef<ChangePasswordDialog>);
  private readonly _us = inject(UserService);
  private readonly _cs = inject(CommonService);

  // Form fields
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  // Password visibility
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  // Form state
  errorMessage = '';
  saving = signal(false);

  // Same password policy as Signup
  private readonly passwordRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,16}$/;

  get passwordsMatch(): boolean {
    return (
      this.newPassword.length > 0 &&
      this.confirmPassword.length > 0 &&
      this.newPassword === this.confirmPassword
    );
  }

  get passwordIsValid(): boolean {
    return this.passwordRegex.test(this.newPassword);
  }

  changePassword(): void {
    this.errorMessage = '';
    this.showCurrentPassword = false;
    this.showNewPassword = false;
    this.showConfirmPassword = false;

    if (this.saving()) {
      return;
    }

    // Validate required fields
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Please complete all required fields.';
      return;
    }

    // Validate new password strength
    if (!this.passwordIsValid) {
      this.errorMessage =
        'Password must be 8–16 characters and include at least one uppercase letter, one lowercase letter, one number, and one special character.';
      return;
    }

    // Prevent reusing the current password
    if (this.currentPassword === this.newPassword) {
      this.errorMessage = 'Your new password must be different from your current password.';
      return;
    }

    // Validate password confirmation
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    const payload = {
      userId: this._cs.getUserProfile.id, // Use the logged-in user's actual ID
      currentPassword: this.currentPassword,
      newPassword: this.newPassword,
    };

    this.saving.set(true);

    this._us.changePassword(payload).subscribe({
      next: (response: ApiResponse) => {
        this.saving.set(false);
        if (response.success) {
          this.dialogRef.close(response.message);
        } else {
          this.errorMessage = response.message || 'Unable to change password. Please try again.';
        }
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage = error?.error?.message || 'Unable to change password. Please try again.';
      },
    });
  }

  toggleCurrentPasswordVisibility(): void {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleNewPasswordVisibility(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  closeDialog(): void {
    if (!this.saving()) {
      this.dialogRef.close();
    }
  }
}
