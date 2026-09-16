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

  private remainingTime = 0;
  private timerStartedAt = 0;

  success(message: string): void {
    this.show('success', message, 5000);
  }

  error(message: string): void {
    this.show('error', message, 10000);
  }

  pause(): void {
    // Nothing to pause
    if (!this.timer) {
      return;
    }

    clearTimeout(this.timer);
    this.timer = undefined;

    const elapsed = Date.now() - this.timerStartedAt;

    this.remainingTime = Math.max(0, this.remainingTime - elapsed);

    this.timerStartedAt = 0;
  }

  resume(): void {
    // No notification
    if (!this.notification()) {
      return;
    }

    // Already running
    if (this.timer) {
      return;
    }

    // Nothing remaining
    if (this.remainingTime <= 0) {
      this.close();
      return;
    }

    this.startTimer();
  }

  close(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = undefined;
    }

    this.remainingTime = 0;
    this.timerStartedAt = 0;

    this.notification.set(null);
  }

  private show(type: NotificationType, message: string, duration: number): void {
    this.close();

    const notification: Notification = {
      id: this.nextId++,
      type,
      message,
      duration,
    };

    this.notification.set(notification);

    this.remainingTime = duration;

    this.startTimer();
  }

  private startTimer(): void {
    if (this.remainingTime <= 0) {
      this.close();
      return;
    }

    this.timerStartedAt = Date.now();

    this.timer = setTimeout(() => {
      this.timer = undefined;
      this.remainingTime = 0;
      this.timerStartedAt = 0;

      this.notification.set(null);
    }, this.remainingTime);
  }
}
