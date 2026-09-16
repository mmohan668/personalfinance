import { Component, TemplateRef, ViewChild, ViewContainerRef, effect, inject } from '@angular/core';

import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';

import { NotificationService } from '../service/notification-service';

@Component({
  selector: 'app-notification',
  standalone: true,
  templateUrl: './notification.html',
  styleUrl: './notification.scss',
})
export class Notification {
  @ViewChild('notificationTemplate')
  notificationTemplate!: TemplateRef<unknown>;

  readonly notificationService = inject(NotificationService);

  private readonly overlay = inject(Overlay);
  private readonly viewContainerRef = inject(ViewContainerRef);

  private overlayRef: OverlayRef | null = null;

  constructor() {
    effect(() => {
      const notification = this.notificationService.item();

      if (notification) {
        this.show();
      } else {
        this.hide();
      }
    });
  }

  private show(): void {
    if (this.overlayRef) {
      return;
    }

    const positionStrategy = this.overlay
      .position()
      .global()
      .centerHorizontally()
      .centerVertically();

    this.overlayRef = this.overlay.create({
      positionStrategy,
      hasBackdrop: false,
      scrollStrategy: this.overlay.scrollStrategies.noop(),

      // Optional - keep this if you want notification
      // to have a higher stacking level.
      panelClass: 'notification-overlay-pane',
    });

    const portal = new TemplatePortal(this.notificationTemplate, this.viewContainerRef);

    this.overlayRef.attach(portal);
  }

  private hide(): void {
    this.overlayRef?.dispose();
    this.overlayRef = null;
  }

  pause(): void {
    this.notificationService.pause();
  }

  resume(): void {
    this.notificationService.resume();
  }

  close(): void {
    this.notificationService.close();
  }

  ngOnDestroy(): void {
    this.overlayRef?.dispose();
  }
}
