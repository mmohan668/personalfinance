import { Component, Inject } from '@angular/core';
import { CommonImportsModule } from '../common-imports/common-imports-module';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-choice-dialog',
  styleUrl: './choice-dialog.scss',
  templateUrl: './choice-dialog.html',
})
export class ChoiceDialog {
  selectedAnswer: 'Yes' | 'No' | null = null;

  constructor(
    private dialogRef: MatDialogRef<ChoiceDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {}

  close(): void {
    this.dialogRef.close(false);
  }

  submit(): void {
    if (!this.selectedAnswer) {
      return;
    }
    this.dialogRef.close(this.selectedAnswer);
  }
}
