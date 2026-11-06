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
  @ViewChild('notificationTemplate') protected notificationTemplate!: TemplateRef<unknown>;

  protected readonly notificationService = inject(NotificationService);
  private readonly overlay = inject(Overlay);
  private readonly viewContainerRef = inject(ViewContainerRef);

  private overlayRef: OverlayRef | null = null;
  private pointerInside = false;
  private focusInside = false;

  constructor() {
    effect(() => {
      const notification = this.notificationService.item();
      if (notification) {
        this.show();
        if (this.pointerInside || this.focusInside) {
          this.notificationService.pause();
        }
      } else {
        this.hide();
      }
    });
  }

  private show(): void {
    if (this.overlayRef) {
      return;
    }

    const positionStrategy = this.overlay.position().global().centerHorizontally().top('20px');

    this.overlayRef = this.overlay.create({
      positionStrategy,
      hasBackdrop: false,
      scrollStrategy: this.overlay.scrollStrategies.noop(),
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
    this.pointerInside = true;
    this.updateTimerState();
  }

  resume(): void {
    this.pointerInside = false;
    this.updateTimerState();
  }

  onFocusIn(): void {
    this.focusInside = true;
    this.updateTimerState();
  }

  onFocusOut(event: FocusEvent): void {
    const container = event.currentTarget as HTMLElement;

    if (!container.contains(event.relatedTarget as Node | null)) {
      this.focusInside = false;
      this.updateTimerState();
    }
  }

  close(): void {
    this.notificationService.close();
  }

  private updateTimerState(): void {
    if (this.pointerInside || this.focusInside) {
      this.notificationService.pause();
    } else {
      this.notificationService.resume();
    }
  }

  ngOnDestroy(): void {
    this.overlayRef?.dispose();
  }
}
