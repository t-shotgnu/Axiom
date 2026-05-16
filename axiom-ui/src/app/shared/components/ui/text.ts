import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-text',
  standalone: true,
  imports: [CommonModule],
  template: `
    @switch (tag) {
      @case ('h1') { <h1 [class]="textClasses"><ng-content></ng-content></h1> }
      @case ('h2') { <h2 [class]="textClasses"><ng-content></ng-content></h2> }
      @case ('h3') { <h3 [class]="textClasses"><ng-content></ng-content></h3> }
      @case ('h4') { <h4 [class]="textClasses"><ng-content></ng-content></h4> }
      @case ('p') { <p [class]="textClasses"><ng-content></ng-content></p> }
      @case ('span') { <span [class]="textClasses"><ng-content></ng-content></span> }
      @case ('label') { <label [class]="textClasses"><ng-content></ng-content></label> }
    }
  `
})
export class TextComponent {
  @Input() variant: 
    | 'headline-xl' | 'headline-lg' | 'headline-md' | 'headline-sm' 
    | 'body-lg' | 'body-md' | 'body-sm' 
    | 'label-lg' | 'label-md' | 'label-sm' = 'body-md';
  @Input() color: 'default' | 'muted' | 'primary' | 'secondary' | 'error' = 'default';
  @Input() weight: 'normal' | 'medium' | 'semibold' | 'bold' | 'black' = 'normal';
  @Input() tag: 'h1' | 'h2' | 'h3' | 'h4' | 'p' | 'span' | 'label' = 'p';
  @Input() customClass: string = '';

  get textClasses(): string {
    let classes = '';

    // Variant mapping
    switch (this.variant) {
      case 'headline-xl': classes += 'font-headline-xl text-headline-xl'; break;
      case 'headline-lg': classes += 'font-headline-lg text-headline-lg'; break;
      case 'headline-md': classes += 'font-headline-md text-headline-md'; break;
      case 'headline-sm': classes += 'font-headline-sm text-headline-sm'; break;
      case 'body-lg':     classes += 'font-body-lg text-body-lg'; break;
      case 'body-md':     classes += 'font-body-md text-body-md'; break;
      case 'body-sm':     classes += 'font-body-sm text-body-sm'; break;
      case 'label-lg':    classes += 'font-label-lg text-label-lg'; break;
      case 'label-md':    classes += 'font-label-md text-label-md'; break;
      case 'label-sm':    classes += 'font-label-sm text-label-sm'; break;
    }

    // Color mapping
    switch (this.color) {
      case 'default':   classes += ' text-on-surface'; break;
      case 'muted':     classes += ' text-on-surface-variant'; break;
      case 'primary':   classes += ' text-primary'; break;
      case 'secondary': classes += ' text-secondary'; break;
      case 'error':     classes += ' text-error'; break;
    }

    // Weight mapping
    switch (this.weight) {
      case 'normal':   classes += ' font-normal'; break;
      case 'medium':   classes += ' font-medium'; break;
      case 'semibold': classes += ' font-semibold'; break;
      case 'bold':     classes += ' font-bold'; break;
      case 'black':    classes += ' font-black'; break;
    }

    return `${classes} ${this.customClass}`.trim();
  }
}
