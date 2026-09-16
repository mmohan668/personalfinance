import {
  Component,
  DestroyRef,
  TemplateRef,
  ViewChild,
  ViewContainerRef,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { LoadingService } from '../service/loading-service';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  template: `
    <ng-template #spinnerTemplate>
      <div class="spinner-container">
        <div class="spinner"></div>
      </div>
    </ng-template>
  `,
  styles: [
    `
      .spinner-container {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 60px;
        height: 60px;
        background: white;
        border-radius: 8px;
      }

      .spinner {
        width: 45px;
        height: 45px;
        border: 5px solid #ddd;
        border-top-color: #333;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
      }

      @keyframes spin {
        to {
          transform: rotate(360deg);
        }
      }
    `,
  ],
})
export class LoadingSpinner {
  @ViewChild('spinnerTemplate')
  spinnerTemplate!: TemplateRef<unknown>;

  private readonly overlay = inject(Overlay);
  private readonly viewContainerRef = inject(ViewContainerRef);
  private readonly loadingService = inject(LoadingService);
  private readonly destroyRef = inject(DestroyRef);

  private overlayRef: OverlayRef | null = null;

  constructor() {
    this.loadingService.loading$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((isLoading) => {
        if (isLoading) {
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
      backdropClass: 'loading-backdrop',
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
