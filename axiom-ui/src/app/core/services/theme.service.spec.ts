import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let listeners: Array<() => void>;
  let prefersDark: boolean;

  beforeEach(() => {
    listeners = [];
    prefersDark = false;
    localStorage.clear();
    document.documentElement.className = '';
    document.documentElement.style.colorScheme = '';

    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn(() => ({
        matches: prefersDark,
        media: '(prefers-color-scheme: dark)',
        addEventListener: (_event: string, listener: () => void) => listeners.push(listener),
        removeEventListener: vi.fn(),
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });

    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.className = '';
    document.documentElement.style.colorScheme = '';
  });

  it('initializes from system preference when no theme is saved', () => {
    prefersDark = true;

    const service = TestBed.inject(ThemeService);

    expect(service.currentTheme()).toBe('system');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(document.documentElement.style.colorScheme).toBe('dark');
  });

  it('persists and applies explicit light and dark themes', () => {
    const service = TestBed.inject(ThemeService);

    service.setTheme('dark');
    expect(localStorage.getItem('axiom_theme')).toBe('dark');
    expect(service.currentTheme()).toBe('dark');
    expect(document.documentElement.classList.contains('dark')).toBe(true);

    service.setTheme('light');
    expect(localStorage.getItem('axiom_theme')).toBe('light');
    expect(service.currentTheme()).toBe('light');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
    expect(document.documentElement.style.colorScheme).toBe('light');
  });

  it('reacts to system preference changes only while using system theme', () => {
    const service = TestBed.inject(ThemeService);
    expect(listeners).toHaveLength(1);

    prefersDark = true;
    listeners[0]();

    expect(document.documentElement.classList.contains('dark')).toBe(true);

    service.setTheme('light');
    prefersDark = true;
    listeners[0]();

    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });
});
