import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DataGrid } from './modules/shared/data-grid/data-grid';


@Component({
  imports: [RouterOutlet, DataGrid],
  selector: 'app-root', styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('cli-web');



}
