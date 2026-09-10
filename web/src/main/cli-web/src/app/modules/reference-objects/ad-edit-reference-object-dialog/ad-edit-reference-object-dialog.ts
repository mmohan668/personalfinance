import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonImportsModule } from '../../shared/common-imports/common-imports-module';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonService } from '../../shared/service/common-service';

@Component({
  selector: 'app-ad-edit-reference-object-dialog',
  standalone: true,
  imports: [CommonImportsModule],
  styleUrl: './ad-edit-reference-object-dialog.scss',
  templateUrl: './ad-edit-reference-object-dialog.html',
})
export class AdEditReferenceObjectDialog {
  private readonly dialogRef = inject(MatDialogRef<AdEditReferenceObjectDialog>);
  readonly data = inject<any>(MAT_DIALOG_DATA);
  form: FormGroup = new FormGroup({});

  constructor(public _cs: CommonService) {
    this.form.addControl(
      'referenceObjectName',
      new FormControl(this.data.mode === 'edit' ? this.data.referenceObjectName : '', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
      ]),
    );
  }

  save(): void {
    this.dialogRef.close({
      action: 'save',
      referenceObjectName: this.form.value.referenceObjectName,
    });
  }

  clear(): void {
    this.form.reset();
  }

  close(): void {
    this.dialogRef.close({
      action: 'close',
    });
  }
}
