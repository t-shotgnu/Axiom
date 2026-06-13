import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled || loading"
      (click)="onClick.emit($event)"
      [class]="buttonClass"
    >
      @if (loading) {
        <span class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
      } @else if (icon) {
        <span class="material-symbols-outlined text-[18px]">{{ icon }}</span>
      }
      <ng-content></ng-content>
    </button>
  `,
})
export class ButtonComponent {
  @Input() variant: 'primary' | 'secondary' | 'inverted' | 'outlined' = 'primary';
  @Input() icon: string = '';
  @Input() disabled: boolean = false;
  @Input() loading: boolean = false;
  @Input() type: 'button' | 'submit' = 'button';
  @Input() customClass: string = '';

  @Output() onClick = new EventEmitter<MouseEvent>();

  get buttonClass(): string {
    const base = 'inline-flex items-center justify-center gap-2 px-4 py-2 text-body-sm rounded font-semibold transition-all duration-150 disabled:opacity-50 disabled:cursor-not-allowed';
    
    let variantClass = '';
    switch (this.variant) {
      case 'primary':
        variantClass = 'bg-primary text-on-primary hover:bg-[#0747A6]';
        break;
      case 'secondary':
        variantClass = 'bg-surface-container-high text-[#0747A6] hover:bg-surface-container-highest';
        break;
      case 'inverted':
        variantClass = 'bg-[#091E42] text-white hover:bg-[#091E42]/90';
        break;
      case 'outlined':
        variantClass = 'bg-transparent border border-outline text-on-surface hover:bg-surface-container-low';
        break;
    }

    return `${base} ${variantClass} ${this.customClass}`;
  }
}
