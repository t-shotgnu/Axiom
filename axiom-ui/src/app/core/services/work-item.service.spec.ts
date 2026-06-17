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

  it('updates work item notes', () => {
    service.updateWorkItemNotes('task-1', 'New notes').subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/work-items/task-1/notes');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ notes: 'New notes' });
    request.flush(null);
  });

  it('updates a work item with partial fields', () => {
    const command = {
      description: 'Renamed task',
      estimatedEffort: 5,
      assigneeId: null,
    };

    service.updateWorkItem('task-1', command).subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/work-items/task-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(command);
    request.flush(null);
  });

  it('deletes a work item', () => {
    service.deleteWorkItem('task-1').subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/work-items/task-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('loads task relationships for a project', () => {
    service.getRelationshipsByProject('project-1').subscribe((relationships) => {
      expect(relationships).toEqual([
        {
          id: 'relationship-1',
          sourceId: 'task-1',
          targetId: 'task-2',
          linkType: 'Blocks',
        },
      ]);
    });

    const request = httpMock.expectOne('/api/task-relationships?projectId=project-1');
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        id: 'relationship-1',
        sourceId: 'task-1',
        targetId: 'task-2',
        linkType: 'Blocks',
      },
    ]);
  });

  it('loads task relationships for a work item', () => {
    service.getRelationshipsByWorkItem('task-1').subscribe((relationships) => {
      expect(relationships).toEqual([]);
    });

    const request = httpMock.expectOne('/api/task-relationships/work-item/task-1');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates and deletes task relationships', () => {
    service
      .createRelationship({ sourceId: 'task-1', targetId: 'task-2', linkType: 'Blocks' })
      .subscribe((id) => {
        expect(id).toBe('relationship-1');
      });

    const createRequest = httpMock.expectOne('/api/task-relationships');
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual({
      sourceId: 'task-1',
      targetId: 'task-2',
      linkType: 'Blocks',
    });
    createRequest.flush('relationship-1');

    service.deleteRelationship('relationship-1').subscribe((result) => {
      expect(result).toBeNull();
    });

    const deleteRequest = httpMock.expectOne('/api/task-relationships/relationship-1');
    expect(deleteRequest.request.method).toBe('DELETE');
    deleteRequest.flush(null);
  });

  it('caches the type hierarchy response for later subscribers', () => {
    const firstResult: Array<Record<string, string[]>> = [];
    const secondResult: Array<Record<string, string[]>> = [];

    service.getTypeHierarchy().subscribe((hierarchy) => firstResult.push(hierarchy));
    const request = httpMock.expectOne('/api/work-items/type-hierarchy');
    expect(request.request.method).toBe('GET');
    request.flush({ Epic: ['Feature'], Task: ['Subtask'] });

    service.getTypeHierarchy().subscribe((hierarchy) => secondResult.push(hierarchy));

    httpMock.expectNone('/api/work-items/type-hierarchy');
    expect(firstResult).toEqual([{ Epic: ['Feature'], Task: ['Subtask'] }]);
    expect(secondResult).toEqual([{ Epic: ['Feature'], Task: ['Subtask'] }]);
  });
});
