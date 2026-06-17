import { ChangeDetectorRef, ElementRef, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ToastService } from '../../../core/services/toast.service';
import { ButtonComponent } from './button';
import { CardComponent } from './card';
import { DialogComponent } from './dialog';
import { InputComponent } from './input';
import { SelectComponent } from './select';
import { ToastContainerComponent } from './toast';

describe('ButtonComponent', () => {
  let fixture: ComponentFixture<ButtonComponent>;
  let component: ButtonComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ButtonComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ButtonComponent);
    component = fixture.componentInstance;
  });

  it('builds variant classes, applies custom classes, and renders icon or loading state', () => {
    component.variant = 'outlined';
    component.icon = 'save';
    component.customClass = 'w-full';
    fixture.detectChanges();

    expect(component.buttonClass).toContain('bg-transparent border border-outline');
    expect(component.buttonClass).toContain('w-full');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('save');

    const loadingFixture = TestBed.createComponent(ButtonComponent);
    loadingFixture.componentInstance.loading = true;
    loadingFixture.detectChanges();

    const button = (loadingFixture.nativeElement as HTMLElement).querySelector('button')!;
    expect(button.disabled).toBe(true);
    expect(button.textContent).toContain('progress_activity');
    expect(button.textContent).not.toContain('save');
    loadingFixture.destroy();
  });

  it('emits click events from the native button', () => {
    const emitted: MouseEvent[] = [];
    component.onClick.subscribe((event) => emitted.push(event));
    fixture.detectChanges();

    (fixture.nativeElement as HTMLElement).querySelector('button')!.click();

    expect(emitted).toHaveLength(1);
  });
});

describe('CardComponent', () => {
  it('returns classes for each visual variant', () => {
    const component = new CardComponent();

    component.variant = 'flat';
    expect(component.cardClass).toContain('bg-surface-container-lowest');

    component.variant = 'low';
    expect(component.cardClass).toContain('bg-surface-container-low');

    component.variant = 'high';
    component.customClass = 'grid gap-md';
    expect(component.cardClass).toContain('bg-surface-container-high');
    expect(component.cardClass).toContain('grid gap-md');
  });
});

describe('DialogComponent', () => {
  it('maps sizes and emits both close outputs', () => {
    const component = new DialogComponent();
    const visibleChanges: boolean[] = [];
    let closeCount = 0;
    component.visible = true;
    component.visibleChange.subscribe((visible) => visibleChanges.push(visible));
    component.onClose.subscribe(() => closeCount++);

    component.size = 'sm';
    expect(component.sizeClass).toBe('max-w-[400px]');
    component.size = 'md';
    expect(component.sizeClass).toBe('max-w-[520px]');
    component.size = 'lg';
    expect(component.sizeClass).toBe('max-w-[800px]');

    component.close();

    expect(component.visible).toBe(false);
    expect(visibleChanges).toEqual([false]);
    expect(closeCount).toBe(1);
  });

  it('closes on backdrop clicks only', () => {
    const component = new DialogComponent();
    component.visible = true;
    const backdrop = document.createElement('div');
    const child = document.createElement('div');
    let closeCount = 0;
    component.onClose.subscribe(() => closeCount++);

    component.onBackdropClick({ target: child, currentTarget: backdrop } as unknown as MouseEvent);
    expect(component.visible).toBe(true);

    component.onBackdropClick({ target: backdrop, currentTarget: backdrop } as unknown as MouseEvent);
    expect(component.visible).toBe(false);
    expect(closeCount).toBe(1);
  });
});

describe('InputComponent', () => {
  it('implements ControlValueAccessor state and callbacks', () => {
    const cdr = { markForCheck: vi.fn() } as unknown as ChangeDetectorRef;
    const component = new InputComponent(cdr);
    const changes: string[] = [];
    let touchCount = 0;
    component.registerOnChange((value: string) => changes.push(value));
    component.registerOnTouched(() => touchCount++);

    component.writeValue('Ada');
    component.setDisabledState(true);
    component.icon = 'search';
    component.customClass = 'text-lg';

    expect(component.val).toBe('Ada');
    expect(component.disabled).toBe(true);
    expect(cdr.markForCheck).toHaveBeenCalled();
    expect(component.inputClasses).toContain('pl-10');
    expect(component.inputClasses).toContain('text-lg');

    component.onInput({ target: { value: 'Grace' } } as unknown as Event);
    component.onBlur();

    expect(component.val).toBe('Grace');
    expect(changes).toEqual(['Grace']);
    expect(touchCount).toBe(2);
  });
});

describe('SelectComponent', () => {
  it('selects values, labels selected options, and closes on document clicks', () => {
    const component = new SelectComponent({ nativeElement: document.createElement('div') } as ElementRef);
    const changes: string[] = [];
    let touchCount = 0;
    component.options = [
      { value: 'new', label: 'New' },
      { value: 'closed', label: 'Closed' },
    ];
    component.registerOnChange((value: string) => changes.push(value));
    component.registerOnTouched(() => touchCount++);

    component.writeValue('new');
    expect(component.selectedLabel).toBe('New');

    component.toggleDropdown();
    expect(component.isOpen).toBe(true);
    expect(component.triggerClasses).toContain('border-primary');

    component.selectOption(component.options[1]!);

    expect(component.val).toBe('closed');
    expect(component.selectedLabel).toBe('Closed');
    expect(changes).toEqual(['closed']);
    expect(touchCount).toBe(1);
    expect(component.isOpen).toBe(false);

    component.toggleDropdown();
    component.closeDropdown();
    expect(component.isOpen).toBe(false);
  });

  it('does not open while disabled', () => {
    const component = new SelectComponent({ nativeElement: document.createElement('div') } as ElementRef);

    component.setDisabledState(true);
    component.toggleDropdown();

    expect(component.isOpen).toBe(false);
    expect(component.triggerClasses).toContain('cursor-not-allowed');
  });
});

describe('ToastContainerComponent', () => {
  it('maps toast icons/classes and removes toasts from the service signal', () => {
    const toasts = signal([
      { id: 'success-1', message: 'Saved', type: 'success' as const },
      { id: 'error-1', message: 'Failed', type: 'error' as const },
      { id: 'info-1', message: 'Heads up', type: 'info' as const },
    ]);

    TestBed.configureTestingModule({
      imports: [ToastContainerComponent],
      providers: [{ provide: ToastService, useValue: { toasts } }],
    });

    const component = TestBed.createComponent(ToastContainerComponent).componentInstance;

    expect(component.getIcon('success')).toBe('check_circle');
    expect(component.getIcon('error')).toBe('error');
    expect(component.getIcon('info')).toBe('info');
    expect(component.getToastClasses(toasts()[0]!)).toContain('bg-green-500/10');
    expect(component.getToastClasses(toasts()[1]!)).toContain('bg-red-500/10');
    expect(component.getToastClasses(toasts()[2]!)).toContain('bg-primary/10');

    component.remove('error-1');

    expect(toasts().map((toast) => toast.id)).toEqual(['success-1', 'info-1']);
  });
});
