import { Component, inject } from '@angular/core';
import { NotificationService } from '../service/notification-service';

@Component({
  imports: [],
  selector: 'app-notification',
  styleUrl: './notification.scss',
  templateUrl: './notification.html',
})
export class Notification {
  readonly notificationService = inject(NotificationService);

  close(): void {
    this.notificationService.close();
  }
}
