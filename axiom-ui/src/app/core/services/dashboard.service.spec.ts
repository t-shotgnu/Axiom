import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads the dashboard summary', () => {
    const expectedSummary = {
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
    };

    service.getSummary().subscribe((summary) => {
      expect(summary).toEqual(expectedSummary);
    });

    const request = httpMock.expectOne('/api/dashboard/summary');
    expect(request.request.method).toBe('GET');
    request.flush(expectedSummary);
  });
});
