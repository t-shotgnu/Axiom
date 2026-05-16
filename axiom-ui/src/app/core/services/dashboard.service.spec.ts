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
    service.getSummary().subscribe((summary) => {
      expect(summary).toEqual({
        activeProjectsCount: 2,
        openTasksCount: 5,
        resolvedTasksCount: 3,
        recentTasks: [],
      });
    });

    const request = httpMock.expectOne('/api/dashboard/summary');
    expect(request.request.method).toBe('GET');
    request.flush({
      activeProjectsCount: 2,
      openTasksCount: 5,
      resolvedTasksCount: 3,
      recentTasks: [],
    });
  });
});
