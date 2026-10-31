import { ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { UserService } from '../../shared/service/user-service';
import { ApiResponse } from '../../shared/types/types';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-profile-picture-dialog',
  imports: [],
  styleUrl: './profile-picture-dialog.scss',
  templateUrl: './profile-picture-dialog.html',
})
export class ProfilePictureDialog implements OnDestroy {
  private readonly dialogRef = inject(MatDialogRef<ProfilePictureDialog>);
  private userService = inject(UserService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly maxFileSize = 10 * 1024 * 1024;

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  errorMessage = '';
  successMessage = '';
  uploading = false;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    this.errorMessage = '';
    this.successMessage = '';

    if (!file) {
      return;
    }

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];

    if (!allowedTypes.includes(file.type)) {
      this.errorMessage = 'Please select a JPG, PNG, or WebP image.';
      input.value = '';
      return;
    }

    if (file.size > this.maxFileSize) {
      this.errorMessage = 'Image size must not exceed 10 MB.';
      input.value = '';
      return;
    }

    this.clearPreview();

    this.selectedFile = file;
    this.previewUrl = URL.createObjectURL(file);
  }

  uploadPicture(): void {
    if (!this.selectedFile || this.uploading) {
      return;
    }

    this.uploading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.userService
      .saveProfilePicture(formData)
      .pipe(
        finalize(() => {
          console.log('Upload request finalized');
          this.uploading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (response: ApiResponse) => {
          console.log('Upload response:', response);

          if (response?.success !== true) {
            this.errorMessage = response?.message ?? 'Failed to upload profile picture.';
            this.cdr.detectChanges();
            return;
          }
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Upload error:', error);

          this.errorMessage = error?.error?.message ?? 'Failed to upload profile picture.';
          this.uploading = false;
        },
        complete: () => {
          console.log('Upload request completed');
          this.uploading = false;
        },
      });
  }

  private clearPreview(): void {
    if (this.previewUrl) {
      URL.revokeObjectURL(this.previewUrl);
      this.previewUrl = null;
    }
  }

  ngOnDestroy(): void {
    this.clearPreview();
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
