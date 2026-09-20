import { NgModule } from '@angular/core';
import { AsyncPipe, CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ToolBar } from '../tool-bar/tool-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { PaginatorModule } from 'primeng/paginator';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { OverlayModule } from '@angular/cdk/overlay';
import { SelectModule } from 'primeng/select';
import { MatDialogModule } from '@angular/material/dialog';
import { CdkDrag, CdkDragHandle } from '@angular/cdk/drag-drop';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';

@NgModule({
  declarations: [],
  imports: [
    TableModule,
    AsyncPipe,
    ToolBar,
    MatSelectModule,
    MatFormFieldModule,
    MatCheckboxModule,
    FormsModule,
    MatMenuModule,
    MatIconModule,
    MatInputModule,
    PaginatorModule,
    CurrencyPipe,
    DatePipe,
    CommonModule,
    InputTextModule,
    OverlayModule,
    DatePickerModule,
    SelectModule,
    MatDialogModule,
    CdkDrag,
    CdkDragHandle,
    ReactiveFormsModule,
    MatRadioModule,
    MatDatepickerModule,
  ],
  providers: [CurrencyPipe, DatePipe],
  exports: [
    TableModule,
    AsyncPipe,
    ToolBar,
    MatSelectModule,
    MatFormFieldModule,
    MatCheckboxModule,
    FormsModule,
    MatMenuModule,
    MatIconModule,
    MatInputModule,
    PaginatorModule,
    CurrencyPipe,
    DatePipe,
    CommonModule,
    InputTextModule,
    OverlayModule,
    DatePickerModule,
    SelectModule,
    MatDialogModule,
    CdkDrag,
    CdkDragHandle,
    ReactiveFormsModule,
    MatRadioModule,
    MatDatepickerModule,
  ],
})
export class CommonImportsModule {}
