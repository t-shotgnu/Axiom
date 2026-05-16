import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { WorkItemService } from './work-item.service';

describe('WorkItemService', () => {
  let service: WorkItemService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(WorkItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads work items for a project', () => {
    service.getWorkItems('project-1').subscribe((items) => {
      expect(items).toEqual([]);
    });

    const request = httpMock.expectOne('/api/work-items?projectId=project-1');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('loads a work item by id', () => {
    service.getWorkItemById('task-1').subscribe();

    const request = httpMock.expectOne('/api/work-items/task-1');
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 'task-1',
      controlNo: 1,
      description: 'Fix bug',
      priority: 1,
      type: 'Bug',
      status: 'New',
      dueDate: '',
      estimatedEffort: 1,
      projectId: 'project-1',
      authorId: 'user-1',
      assigneeId: '',
    });
  });

  it('creates a work item', () => {
    const command = {
      description: 'Build feature',
      priority: 2,
      type: 'Task',
      status: 'New',
      projectId: 'project-1',
    };

    service.createWorkItem(command).subscribe((id) => {
      expect(id).toBe('task-1');
    });

    const request = httpMock.expectOne('/api/work-items');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(command);
    request.flush('task-1');
  });

  it('assigns a work item', () => {
    service.assignWorkItem('task-1', { assigneeId: 'user-2' }).subscribe();

    const request = httpMock.expectOne('/api/work-items/task-1/assignee');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ assigneeId: 'user-2' });
    request.flush(null);
  });

  it('updates a work item status', () => {
    service.updateWorkItemStatus('task-1', { status: 'Resolved' }).subscribe();

    const request = httpMock.expectOne('/api/work-items/task-1/status');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ status: 'Resolved' });
    request.flush(null);
  });
});
