import { NgModule } from '@angular/core';
import { AsyncPipe, CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ToolBar } from '../tool-bar/tool-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FormsModule } from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PaginatorModule } from 'primeng/paginator';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { OverlayModule } from '@angular/cdk/overlay';

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
  ],
})
export class CommonImportsModule {}
