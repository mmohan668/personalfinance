import { Component, inject } from '@angular/core';
import { CommonImportsModule } from '../common-imports/common-imports-module';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConfirmationDialogData } from '../types/types';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-confirmation-dialog',
  styleUrl: './confirmation-dialog.scss',
  templateUrl: './confirmation-dialog.html',
})
export class ConfirmationDialog {
  private dialogRef = inject(MatDialogRef<ConfirmationDialog>);
  protected data = inject<ConfirmationDialogData>(MAT_DIALOG_DATA);

  constructor() {}

  yes(): void {
    this.dialogRef.close(true);
  }

  no(): void {
    this.dialogRef.close(false);
  }

  ok(): void {
    this.dialogRef.close(false);
  }

  close(): void {
    this.dialogRef.close(false);
  }
}
