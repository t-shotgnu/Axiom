import { ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { DashboardService, DashboardSummary } from '../../core/services/dashboard.service';
import { DashboardComponent } from './dashboard';

describe('DashboardComponent', () => {
  let dashboardService: { getSummary: ReturnType<typeof vi.fn> };
  let cdr: { markForCheck: ReturnType<typeof vi.fn> };
  let summary$: Subject<DashboardSummary>;
  let component: DashboardComponent;

  beforeEach(() => {
    summary$ = new Subject<DashboardSummary>();
    dashboardService = {
      getSummary: vi.fn(() => summary$.asObservable()),
    };
    cdr = {
      markForCheck: vi.fn(),
    };
    component = new DashboardComponent(
      dashboardService as unknown as DashboardService,
      cdr as unknown as ChangeDetectorRef,
    );
  });

  it('loads summary counts and recent tasks on init', () => {
    component.ngOnInit();

    summary$.next({
      activeProjectsCount: 2,
      openTasksCount: 5,
      resolvedTasksCount: 3,
      recentTasks: [],
    });
    summary$.complete();

    expect(component.activeProjectsCount).toBe(2);
    expect(component.openTasksCount).toBe(5);
    expect(component.resolvedTasksCount).toBe(3);
    expect(component.recentTasks).toEqual([]);
    expect(component.loading).toBe(false);
    expect(cdr.markForCheck).toHaveBeenCalled();
  });

  it('uses an empty recent task list when the API omits it', () => {
    component.ngOnInit();

    summary$.next({
      activeProjectsCount: 0,
      openTasksCount: 0,
      resolvedTasksCount: 0,
      recentTasks: undefined as unknown as [],
    });
    summary$.complete();

    expect(component.recentTasks).toEqual([]);
  });

  it('stops loading after errors', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    component.ngOnInit();

    summary$.error(new Error('network failed'));

    expect(component.loading).toBe(false);
    expect(cdr.markForCheck).toHaveBeenCalled();
    consoleError.mockRestore();
  });
});
