import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  imports: [FormsModule],
  selector: 'app-bulk-upload',
  styleUrl: './bulk-upload.scss',
  templateUrl: './bulk-upload.html',
})
export class BulkUpload {
  uploadTypes = [
    { value: 'reference-values', label: 'Reference Values' },
    { value: 'categories', label: 'Categories' },
    { value: 'subcategories', label: 'Subcategories' },
    { value: 'financial-transactions', label: 'Financial Transactions' },
  ];

  selectedUploadType = '';
  selectedFile: File | null = null;
  fileError = '';

  onFileSelected(event: Event): void {
    this.fileError = '';

    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      this.selectedFile = null;
      return;
    }

    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      this.selectedFile = null;
      this.fileError = 'Only .xlsx files are allowed.';
      input.value = '';
      return;
    }

    this.selectedFile = file;
  }

  downloadTemplate(): void {
    if (!this.selectedUploadType) {
      return;
    }

    // TODO: Replace with API call to download
    // the template for the selected upload type.
    console.log('Download template:', this.selectedUploadType);
  }

  reset(): void {
    this.selectedUploadType = '';
    this.selectedFile = null;
    this.fileError = '';
  }

  submit(): void {
    if (!this.selectedUploadType || !this.selectedFile) {
      return;
    }

    // TODO: Replace with API call.
    const formData = new FormData();

    formData.append('uploadType', this.selectedUploadType);
    formData.append('file', this.selectedFile);

    console.log('Submitting bulk upload', formData);
  }

  get canSubmit(): boolean {
    return !!this.selectedUploadType && !!this.selectedFile;
  }
}
