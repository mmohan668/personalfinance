import { Component, TemplateRef, ViewChild, ViewContainerRef, effect, inject } from '@angular/core';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { LoadingService } from '../service/loading-service';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  templateUrl: './loading-spinner.html',
  styleUrl: './loading-spinner.scss',
})
export class LoadingSpinner {
  @ViewChild('spinnerTemplate') protected spinnerTemplate!: TemplateRef<unknown>;

  private readonly overlay = inject(Overlay);
  private readonly viewContainerRef = inject(ViewContainerRef);
  private readonly loadingService = inject(LoadingService);

  private overlayRef: OverlayRef | null = null;

  constructor() {
    effect(() => {
      if (this.loadingService.loading()) {
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
      hasBackdrop: true,
      backdropClass: ['cdk-overlay-dark-backdrop', 'loading-backdrop'],
      scrollStrategy: this.overlay.scrollStrategies.block(),
    });

    const portal = new TemplatePortal(this.spinnerTemplate, this.viewContainerRef);

    this.overlayRef.attach(portal);
  }

  private hide(): void {
    this.overlayRef?.dispose();
    this.overlayRef = null;
  }
}
