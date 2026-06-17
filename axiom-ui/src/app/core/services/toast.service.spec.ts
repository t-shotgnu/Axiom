import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    vi.useFakeTimers();

    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('adds a success toast by default and removes it after the duration', () => {
    service.show('Saved', undefined, 1000);

    expect(service.toasts()).toEqual([
      expect.objectContaining({ message: 'Saved', type: 'success' }),
    ]);

    vi.advanceTimersByTime(1000);

    expect(service.toasts()).toEqual([]);
  });

  it('keeps newer toasts when an older toast expires', () => {
    service.error('First', 1000);
    service.info('Second', 2000);

    expect(service.toasts()).toHaveLength(2);

    vi.advanceTimersByTime(1000);

    expect(service.toasts()).toEqual([
      expect.objectContaining({ message: 'Second', type: 'info' }),
    ]);

    vi.advanceTimersByTime(1000);
    expect(service.toasts()).toEqual([]);
  });
});
