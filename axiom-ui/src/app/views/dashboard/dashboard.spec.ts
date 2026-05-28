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

  function summary(overrides: Partial<DashboardSummary> = {}): DashboardSummary {
    return {
      activeProjectsCount: 2,
      totalTasksCount: 8,
      openTasksCount: 5,
      inProgressTasksCount: 2,
      resolvedTasksCount: 3,
      unassignedTasksCount: 1,
      overdueTasksCount: 0,
      completionPercent: 38,
      statusBreakdown: [],
      typeBreakdown: [],
      priorityBreakdown: [],
      projectProgress: [],
      assigneeWorkload: [],
      recentTasks: [],
      ...overrides,
    };
  }

  it('loads summary counts and recent tasks on init', () => {
    component.ngOnInit();

    summary$.next(summary());
    summary$.complete();

    expect(component.activeProjectsCount).toBe(2);
    expect(component.totalTasksCount).toBe(8);
    expect(component.openTasksCount).toBe(5);
    expect(component.inProgressTasksCount).toBe(2);
    expect(component.resolvedTasksCount).toBe(3);
    expect(component.unassignedTasksCount).toBe(1);
    expect(component.overdueTasksCount).toBe(0);
    expect(component.completionPercent).toBe(38);
    expect(component.recentTasks).toEqual([]);
    expect(component.loading).toBe(false);
    expect(cdr.markForCheck).toHaveBeenCalled();
  });

  it('uses an empty recent task list when the API omits it', () => {
    component.ngOnInit();

    summary$.next(summary({
      recentTasks: undefined as unknown as [],
    }));
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
