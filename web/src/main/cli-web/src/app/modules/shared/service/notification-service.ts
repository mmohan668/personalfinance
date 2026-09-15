import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error';

export interface Notification {
  id: number;
  type: NotificationType;
  message: string;
  duration: number;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly notification = signal<Notification | null>(null);

  readonly item = this.notification.asReadonly();

  private nextId = 1;

  private timer?: ReturnType<typeof setTimeout>;

  /**
   * Show success notification.
   * Automatically closes after 5 seconds.
   */
  success(message: string): void {
    this.show('success', message, 5000);
  }

  /**
   * Show error notification.
   * Automatically closes after 10 seconds.
   */
  error(message: string): void {
    this.show('error', message, 10000);
  }

  /**
   * Manually close the current notification.
   */
  close(): void {
    // Cancel existing timer
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = undefined;
    }

    // Remove notification
    this.notification.set(null);
  }

  /**
   * Show a new notification.
   *
   * If another notification is already visible,
   * it will be closed immediately.
   */
  private show(type: NotificationType, message: string, duration: number): void {
    // Close existing notification immediately.
    // This also clears its timer.
    this.close();

    const notification: Notification = {
      id: this.nextId++,
      type,
      message,
      duration,
    };

    // Show new notification
    this.notification.set(notification);

    // Start new timer
    this.timer = setTimeout(() => {
      this.close();
    }, duration);
  }
}
