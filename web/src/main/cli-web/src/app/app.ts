import { Component, signal } from '@angular/core';
import { Menu } from './modules/shared/menu/menu';
import { RouterOutlet } from '@angular/router';
import { Notification } from './modules/shared/notification/notification';
import { LoadingSpinner } from './modules/shared/loading-spinner/loading-spinner';

@Component({
  imports: [Menu, RouterOutlet, Notification, LoadingSpinner],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('cli-web');
}
