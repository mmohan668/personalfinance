import { Component, inject, signal } from '@angular/core';
import { Menu } from './modules/shared/menu/menu';
import { RouterOutlet } from '@angular/router';
import { Notification } from './modules/shared/notification/notification';
import { LoadingSpinner } from './modules/shared/loading-spinner/loading-spinner';
import { SystemConfigService } from './modules/shared/service/system-config-service';

@Component({
  imports: [Menu, RouterOutlet, Notification, LoadingSpinner],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('cli-web');
  private _scs = inject(SystemConfigService);
  constructor() {
    this._scs.currency.set('USD');
    this._scs.dateFormat.set('MM/dd/yyyy');
    this._scs.dateTimeFormat.set('MM/dd/yyyy' + ' HH:mm:ss');
  }
}
