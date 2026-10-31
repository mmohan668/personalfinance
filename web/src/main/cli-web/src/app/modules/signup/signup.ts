import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiResponse } from '../shared/types/types';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../shared/service/auth-service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './signup.html',
  styleUrl: './signup.scss',
})
export class Signup {
  private readonly _as = inject(AuthService);

  // Form fields
  username = '';
  displayName = '';
  email = '';
  password = '';
  confirmPassword = '';
  acceptTerms = false;

  // Feedback messages
  errorMessage = signal('');
  successMessage = signal('');

  // Password visibility
  showPassword = false;
  showConfirmPassword = false;

  onSignup(event: SubmitEvent): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    event.preventDefault();

    // Validate required fields
    if (
      !this.username.trim() ||
      !this.displayName.trim() ||
      !this.email.trim() ||
      !this.password ||
      !this.confirmPassword
    ) {
      this.errorMessage.set('Please complete all required fields.');
      return;
    }

    const usernameRegex = /^[A-Za-z0-9@_-]{3,30}$/;

    if (!usernameRegex.test(this.username.trim())) {
      this.errorMessage.set(
        'Username must be 3–30 characters and contain only uppercase letters (A–Z), numbers (0–9), @, _ or -.',
      );
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailRegex.test(this.email.trim())) {
      this.errorMessage.set('Please enter a valid email address.');
      return;
    }

    // Validate password strength
    const passwordRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,16}$/;

    if (!passwordRegex.test(this.password)) {
      this.errorMessage.set(
        'Password must be 8–16 characters and include at least one uppercase letter, one lowercase letter, one number, and one special character.',
      );
      return;
    }

    // Check password confirmation
    if (this.password !== this.confirmPassword) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    // Validate terms acceptance
    if (!this.acceptTerms) {
      this.errorMessage.set('Please accept the Terms of Service and Privacy Policy.');
      return;
    }

    const user: any = {
      id: null,
      username: this.username,
      displayName: this.displayName,
      email: this.email,
      password: this.password,
    };
    firstValueFrom(this._as.signUp(user)).then((response: ApiResponse) => {
      if (response.success) {
        this.successMessage.set(response.message);
        this.username = '';
        this.displayName = '';
        this.email = '';
        this.password = '';
        this.confirmPassword = '';
        this.acceptTerms = false;
        this.showPassword = false;
        this.showConfirmPassword = false;
      } else {
        this.errorMessage.set(response.message);
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }
}
