import { Component, signal } from '@angular/core';
import { Menu } from './modules/shared/menu/menu';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [Menu, RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('cli-web');
}
