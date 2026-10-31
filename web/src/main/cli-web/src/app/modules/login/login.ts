import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../shared/service/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  errorMessage = '';
  isLoading = signal<boolean>(false);

  onLogin(): void {
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
          this.isLoading.set(false);

          void this.router.navigate(['/']);
        },
        error: (error) => {
          this.isLoading.set(false);
        },
      });
  }
}
