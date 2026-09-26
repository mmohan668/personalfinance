import { Component, inject } from '@angular/core';
import { CommonImportsModule } from '../common-imports/common-imports-module';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  imports: [CommonImportsModule],
  selector: 'app-choice-dialog',
  styleUrl: './choice-dialog.scss',
  templateUrl: './choice-dialog.html',
})
export class ChoiceDialog {
  protected selectedAnswer: 'Yes' | 'No' | null = null;
  protected dialogRef = inject(MatDialogRef<ChoiceDialog>);
  protected data = inject<any>(MAT_DIALOG_DATA);

  submit(): void {
    if (!this.selectedAnswer) {
      return;
    }
    this.dialogRef.close(this.selectedAnswer);
  }
}
