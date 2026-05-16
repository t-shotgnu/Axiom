import { Component, Input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true
    }
  ],
  template: `
    <div class="flex flex-col gap-xs w-full">
      @if (label) {
        <label [for]="id" class="font-label-md text-on-surface font-semibold">{{ label }}</label>
      }
      <div class="relative flex items-center w-full">
        @if (icon) {
          <span class="material-symbols-outlined absolute left-3 text-on-surface-variant text-[20px] pointer-events-none">{{ icon }}</span>
        }
        <input
          [id]="id"
          [type]="type"
          [value]="val"
          (input)="onInput($event)"
          (blur)="onBlur()"
          [placeholder]="placeholder"
          [disabled]="disabled"
          [class]="inputClasses"
        />
      </div>
    </div>
  `
})
export class InputComponent implements ControlValueAccessor {
  @Input() id = 'input-' + Math.random().toString(36).substr(2, 9);
  @Input() label = '';
  @Input() type = 'text';
  @Input() placeholder = '';
  @Input() icon = '';
  @Input() disabled = false;
  @Input() customClass = '';

  val: any = '';

  onChange: any = () => {};
  onTouch: any = () => {};

  get inputClasses(): string {
    const base = 'w-full px-md py-sm border border-outline-variant rounded focus:outline-none focus:border-primary bg-surface text-on-surface transition-all duration-150 disabled:opacity-50 disabled:cursor-not-allowed';
    const padding = this.icon ? 'pl-10' : '';
    return `${base} ${padding} ${this.customClass}`.trim();
  }

  writeValue(value: any): void {
    this.val = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouch = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.val = value;
    this.onChange(value);
    this.onTouch();
  }

  onBlur(): void {
    this.onTouch();
  }
}
