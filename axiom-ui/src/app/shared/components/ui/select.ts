import { Component, Input, forwardRef, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';

export interface SelectOption {
  value: any;
  label: string;
  icon?: string;
  colorClass?: string;
}

@Component({
  selector: 'app-select',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true
    }
  ],
  template: `
    <div class="flex flex-col gap-xs w-full relative" (click)="$event.stopPropagation()">
      @if (label) {
        <label class="font-label-md text-on-surface font-semibold select-none">{{ label }}</label>
      }
      
      <!-- Select Trigger Button -->
      <div
        (click)="toggleDropdown()"
        [class]="triggerClasses"
        [attr.aria-disabled]="disabled"
      >
        <span class="truncate font-semibold select-none">
          {{ selectedLabel || placeholder || 'Select option' }}
        </span>
        <span class="material-symbols-outlined text-on-surface-variant text-[20px] transition-transform duration-200 shrink-0"
          [class.rotate-180]="isOpen"
        >
          expand_more
        </span>
      </div>

      <!-- Floating Dropdown Overlay -->
      @if (isOpen && !disabled) {
        <div
          class="absolute top-full left-0 right-0 mt-xs bg-surface-container-lowest border border-outline-variant rounded-xl shadow-xl z-50 max-h-[220px] overflow-y-auto custom-scrollbar p-sm flex flex-col gap-[2px]"
        >
          @if (options.length === 0) {
            <div class="text-xs text-on-surface-variant text-center py-sm select-none">No options available</div>
          }
          
          @for (opt of options; track opt.value) {
            <div
              (click)="selectOption(opt)"
              [class.bg-primary/6]="val === opt.value"
              [class.text-primary]="val === opt.value"
              [class.font-bold]="val === opt.value"
              class="flex items-center justify-between gap-sm px-md py-sm rounded-lg hover:bg-surface-container-high cursor-pointer transition-colors text-xs font-semibold text-on-surface"
            >
              <span class="truncate">{{ opt.label }}</span>
              @if (val === opt.value) {
                <span class="material-symbols-outlined text-[16px] font-bold">check</span>
              }
            </div>
          }
        </div>
      }
    </div>
  `
})
export class SelectComponent implements ControlValueAccessor {
  @Input() label = '';
  @Input() placeholder = '';
  @Input() options: SelectOption[] = [];
  @Input() disabled = false;
  @Input() customClass = '';

  val: any = null;
  isOpen = false;

  onChange: any = () => {};
  onTouch: any = () => {};

  constructor(private elementRef: ElementRef) {}

  get selectedLabel(): string {
    const matched = this.options.find(o => o.value === this.val);
    return matched ? matched.label : '';
  }

  get triggerClasses(): string {
    const base = 'w-full flex items-center justify-between px-md py-sm border border-outline-variant rounded bg-surface text-on-surface text-xs font-medium cursor-pointer transition-all duration-150 select-none';
    const active = this.isOpen ? 'border-primary ring-1 ring-primary/20' : 'hover:border-outline';
    const disabledState = this.disabled ? 'opacity-50 cursor-not-allowed pointer-events-none' : '';
    return `${base} ${active} ${disabledState} ${this.customClass}`.trim();
  }

  toggleDropdown(): void {
    if (this.disabled) return;
    this.isOpen = !this.isOpen;
  }

  selectOption(opt: SelectOption): void {
    this.val = opt.value;
    this.onChange(opt.value);
    this.onTouch();
    this.isOpen = false;
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

  @HostListener('document:click')
  closeDropdown(): void {
    this.isOpen = false;
  }
}
