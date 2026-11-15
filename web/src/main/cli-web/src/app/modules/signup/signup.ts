import {
  Component,
  ElementRef,
  HostListener,
  inject,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';

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
export class Signup implements OnInit {
  private readonly _as = inject(AuthService);

  // Reference to password wrapper
  @ViewChild('passwordWrapper')
  passwordWrapper?: ElementRef<HTMLDivElement>;

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

  // Password suggestion popup
  showPasswordSuggestion = false;
  suggestedPassword = '';
  copySuccess = false;

  ngOnInit(): void {
    this.generateSuggestedPassword();
  }

  // Open password suggestion popup
  openPasswordSuggestion(): void {
    this.showPasswordSuggestion = true;
  }

  // Close popup only when clicking outside password wrapper
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

  // Signup
  onSignup(event: SubmitEvent): void {
    event.preventDefault();

    this.errorMessage.set('');
    this.successMessage.set('');

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

    // Validate username
    const usernameRegex = /^[A-Za-z0-9@_-]{3,30}$/;

    if (!usernameRegex.test(this.username.trim())) {
      this.errorMessage.set(
        'Username must be 3–30 characters and contain only uppercase letters (A–Z), numbers (0–9), @, _ or -.',
      );
      return;
    }

    // Validate email
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

    // User object
    const user: any = {
      id: null,
      username: this.username.trim(),
      displayName: this.displayName.trim(),
      email: this.email.trim(),
      password: this.password,
    };

    // Call signup API
    firstValueFrom(this._as.signUp(user))
      .then((response: ApiResponse) => {
        if (response.success) {
          this.successMessage.set(response.message);

          // Reset form fields
          this.username = '';
          this.displayName = '';
          this.email = '';
          this.password = '';
          this.confirmPassword = '';
          this.acceptTerms = false;

          // Reset password visibility
          this.showPassword = false;
          this.showConfirmPassword = false;

          // Close suggestion popup
          this.showPasswordSuggestion = false;
          this.copySuccess = false;

          // Generate a fresh suggestion
          this.generateSuggestedPassword();
        } else {
          this.errorMessage.set(response.message);
        }
      })
      .catch((error: unknown) => {
        console.error('Signup error:', error);

        this.errorMessage.set('Something went wrong. Please try again later.');
      });
  }

  // Toggle password visibility
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  // Toggle confirm password visibility
  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  // Generate strong password
  generateSuggestedPassword(): void {
    const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lowercase = 'abcdefghijklmnopqrstuvwxyz';
    const numbers = '0123456789';
    const symbols = '!@#$%^&*_-';

    const allCharacters = uppercase + lowercase + numbers + symbols;

    // Guarantee at least one character from each category
    const passwordCharacters = [
      this.randomCharacter(uppercase),
      this.randomCharacter(lowercase),
      this.randomCharacter(numbers),
      this.randomCharacter(symbols),
    ];

    // Fill remaining characters (16 characters total)
    while (passwordCharacters.length < 16) {
      passwordCharacters.push(this.randomCharacter(allCharacters));
    }

    // Securely shuffle password characters
    for (let i = passwordCharacters.length - 1; i > 0; i--) {
      const j = this.secureRandomIndex(i + 1);

      [passwordCharacters[i], passwordCharacters[j]] = [
        passwordCharacters[j],
        passwordCharacters[i],
      ];
    }

    this.suggestedPassword = passwordCharacters.join('');

    // Reset copy feedback
    this.copySuccess = false;
  }

  // Generate random character
  private randomCharacter(characters: string): string {
    return characters[this.secureRandomIndex(characters.length)];
  }

  // Cryptographically secure random index
  private secureRandomIndex(max: number): number {
    const random = new Uint32Array(1);
    const limit = Math.floor(0x100000000 / max) * max;

    do {
      crypto.getRandomValues(random);
    } while (random[0] >= limit);

    return random[0] % max;
  }

  // Use suggested password
  useSuggestedPassword(): void {
    if (!this.suggestedPassword) {
      return;
    }

    // Populate both fields
    this.password = this.suggestedPassword;
    this.confirmPassword = this.suggestedPassword;

    // Reset visibility
    this.showPassword = false;
    this.showConfirmPassword = false;

    // Close popup
    this.showPasswordSuggestion = false;

    // Reset copy feedback
    this.copySuccess = false;
  }

  // Copy suggested password
  async copySuggestedPassword(): Promise<void> {
    if (!this.suggestedPassword) {
      return;
    }

    try {
      if (navigator.clipboard && window.isSecureContext) {
        // Modern Clipboard API
        await navigator.clipboard.writeText(this.suggestedPassword);
      } else {
        // Fallback for unsupported environments
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

      // Show copy success without closing popup
      this.copySuccess = true;
    } catch (error) {
      console.error('Clipboard error:', error);

      this.copySuccess = false;

      this.errorMessage.set('Unable to copy password. Please copy it manually.');
    }
  }
}
