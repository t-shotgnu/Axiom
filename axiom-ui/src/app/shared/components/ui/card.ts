import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="cardClass">
      <ng-content></ng-content>
    </div>
  `
})
export class CardComponent {
  @Input() variant: 'flat' | 'low' | 'high' = 'flat';
  @Input() customClass: string = '';

  get cardClass(): string {
    const base = 'rounded-xl border border-outline-variant p-lg shadow-sm transition-all';
    let variantClass = '';
    switch (this.variant) {
      case 'flat':
        variantClass = 'bg-surface-container-lowest text-on-surface';
        break;
      case 'low':
        variantClass = 'bg-surface-container-low text-on-surface';
        break;
      case 'high':
        variantClass = 'bg-surface-container-high text-on-surface';
        break;
    }
    return `${base} ${variantClass} ${this.customClass}`;
  }
}
