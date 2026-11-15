import {
  Component,
  ElementRef,
  HostListener,
  inject,
  Input,
  signal,
  ViewChild,
} from '@angular/core';

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

  @Input() forcePasswordChange = false;

  // Reference to the new-password wrapper.
  // This is the same approach used on the Signup page.
  @ViewChild('passwordWrapper')
  passwordWrapper?: ElementRef<HTMLDivElement>;

  // Form fields
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  // Password visibility
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  // Suggested password popup
  showPasswordSuggestion = false;
  suggestedPassword = '';
  copySuccess = false;
  copyErrorMessage = '';

  // Form state
  errorMessage = '';
  saving = signal(false);

  // Password policy
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

  // Open password suggestion popup.
  openPasswordSuggestion(): void {
    if (this.saving()) {
      return;
    }

    if (!this.suggestedPassword) {
      this.generateSuggestedPassword();
    }

    this.showPasswordSuggestion = true;
  }

  // Close popup only when clicking outside the password wrapper.
  // Copy, refresh and use buttons are inside the wrapper, so they do not close it.
  @HostListener('document:pointerdown', ['$event'])
  onDocumentPointerDown(event: PointerEvent): void {
    if (!this.showPasswordSuggestion) {
      return;
    }

    const wrapper = this.passwordWrapper?.nativeElement;
    const target = event.target;

    if (wrapper && target instanceof Node && !wrapper.contains(target)) {
      this.showPasswordSuggestion = false;
    }
  }

  // Generate a strong password matching the Signup page policy.
  generateSuggestedPassword(): void {
    const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lowercase = 'abcdefghijklmnopqrstuvwxyz';
    const numbers = '0123456789';
    const symbols = '!@#$%^&*_-';

    const allCharacters = uppercase + lowercase + numbers + symbols;

    // Guarantee at least one character from each category.
    const passwordCharacters = [
      this.randomCharacter(uppercase),
      this.randomCharacter(lowercase),
      this.randomCharacter(numbers),
      this.randomCharacter(symbols),
    ];

    // Fill remaining characters to make a 16-character password.
    while (passwordCharacters.length < 16) {
      passwordCharacters.push(this.randomCharacter(allCharacters));
    }

    // Securely shuffle password characters.
    for (let i = passwordCharacters.length - 1; i > 0; i--) {
      const j = this.secureRandomIndex(i + 1);

      [passwordCharacters[i], passwordCharacters[j]] = [
        passwordCharacters[j],
        passwordCharacters[i],
      ];
    }

    this.suggestedPassword = passwordCharacters.join('');

    // Reset copy feedback.
    this.copySuccess = false;
    this.copyErrorMessage = '';
  }

  // Generate a random character.
  private randomCharacter(characters: string): string {
    return characters[this.secureRandomIndex(characters.length)];
  }

  // Cryptographically secure random index with rejection sampling.
  private secureRandomIndex(max: number): number {
    const random = new Uint32Array(1);
    const limit = Math.floor(0x100000000 / max) * max;

    do {
      crypto.getRandomValues(random);
    } while (random[0] >= limit);

    return random[0] % max;
  }

  // Copy suggested password without closing the popup.
  async copySuggestedPassword(): Promise<void> {
    if (!this.suggestedPassword) {
      return;
    }

    this.copySuccess = false;
    this.copyErrorMessage = '';

    try {
      if (navigator.clipboard && window.isSecureContext) {
        // Modern Clipboard API.
        await navigator.clipboard.writeText(this.suggestedPassword);
      } else {
        // Fallback for unsupported environments.
        const textarea = document.createElement('textarea');

        textarea.value = this.suggestedPassword;
        textarea.setAttribute('readonly', '');

        textarea.style.position = 'fixed';
        textarea.style.left = '-9999px';
        textarea.style.top = '0';
        textarea.style.opacity = '0';

        document.body.appendChild(textarea);

        textarea.focus();
        textarea.select();

        const copied = document.execCommand('copy');

        textarea.remove();

        if (!copied) {
          throw new Error('Clipboard copy failed');
        }
      }

      // Keep popup open and show success message.
      this.copySuccess = true;
      this.copyErrorMessage = '';
    } catch (error) {
      console.error('Clipboard error:', error);

      this.copySuccess = false;
      this.copyErrorMessage = 'Unable to copy password. Please select and copy it manually.';
    }
  }

  // Apply suggested password to both fields.
  useSuggestedPassword(): void {
    if (!this.suggestedPassword) {
      return;
    }

    this.newPassword = this.suggestedPassword;
    this.confirmPassword = this.suggestedPassword;

    this.showNewPassword = false;
    this.showConfirmPassword = false;

    this.copySuccess = false;
    this.copyErrorMessage = '';
    this.showPasswordSuggestion = false;
    this.errorMessage = '';
  }

  // Change password.
  changePassword(): void {
    this.errorMessage = '';

    if (this.saving()) {
      return;
    }

    // Validate required fields.
    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Please complete all required fields.';
      return;
    }

    // Validate new password strength.
    if (!this.passwordIsValid) {
      this.errorMessage =
        'Password must be 8–16 characters and include at least one uppercase letter, one lowercase letter, one number, and one special character.';
      return;
    }

    // Prevent reusing the current password.
    if (this.currentPassword === this.newPassword) {
      this.errorMessage = 'Your new password must be different from your current password.';
      return;
    }

    // Validate password confirmation.
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    const payload = {
      userId: this._cs.loginUserId,
      currentPassword: this.currentPassword,
      newPassword: this.newPassword,
    };

    this.showPasswordSuggestion = false;
    this.showCurrentPassword = false;
    this.showNewPassword = false;
    this.showConfirmPassword = false;

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

  // Toggle password visibility.
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
