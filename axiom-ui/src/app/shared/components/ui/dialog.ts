import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from './card';

@Component({
  selector: 'app-dialog',
  standalone: true,
  imports: [CommonModule, CardComponent],
  template: `
    @if (visible) {
      <div
        class="fixed inset-0 z-50 flex justify-center bg-black/50 backdrop-blur-sm p-4"
        [class.items-center]="position === 'center'"
        [class.items-start]="position === 'top'"
        [class.pt-4]="position === 'top'"
        [class.pt-24]="position === 'top-lg'"
        (click)="onBackdropClick($event)"
      >
        <app-card
          variant="flat"
          [customClass]="'w-full flex flex-col max-h-full overflow-hidden p-0 shadow-xl bg-surface-container-lowest ' + sizeClass"
        >
          <!-- Header -->
          <div class="flex justify-between items-center p-md border-b border-outline-variant">
            <h2 class="font-headline-sm text-headline-sm text-on-surface">{{ title }}</h2>
            <button
              type="button"
              (click)="close()"
              class="text-on-surface-variant hover:bg-surface-container-low p-xs rounded transition-colors material-symbols-outlined"
            >
              close
            </button>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto p-lg flex flex-col gap-lg">
            <ng-content></ng-content>
          </div>

          <!-- Footer -->
          <div
            class="flex justify-end gap-sm p-md border-t border-outline-variant"
            [class.bg-surface-container-low]="footerStyle === 'default'"
            [class.bg-transparent]="footerStyle === 'plain'"
          >
            <ng-content select="[footer]"></ng-content>
          </div>
        </app-card>
      </div>
    }
  `,
})
export class DialogComponent {
  @Input() visible = false;
  @Input() title = '';
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() position: 'center' | 'top' | 'top-lg' = 'center';
  @Input() footerStyle: 'default' | 'plain' = 'default';

  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() onClose = new EventEmitter<void>();

  get sizeClass(): string {
    switch (this.size) {
      case 'sm': return 'max-w-[400px]';
      case 'lg': return 'max-w-[800px]';
      case 'md':
      default:
        return 'max-w-[520px]';
    }
  }

  close(): void {
    this.visible = false;
    this.visibleChange.emit(false);
    this.onClose.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.close();
    }
  }
}
