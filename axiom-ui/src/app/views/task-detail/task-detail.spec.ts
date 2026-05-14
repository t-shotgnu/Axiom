import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { WorkItem, WorkItemService } from '../../core/services/work-item.service';
import { TaskDetailComponent } from './task-detail';

describe('TaskDetailComponent', () => {
  let workItemService: {
    getWorkItemById: ReturnType<typeof vi.fn>;
    updateWorkItemStatus: ReturnType<typeof vi.fn>;
    assignWorkItem: ReturnType<typeof vi.fn>;
  };
  let component: TaskDetailComponent;

  const task: WorkItem = {
    id: 'task-1',
    controlNo: 1,
    description: 'Build tests',
    priority: 2,
    type: 'Task',
    status: 'Active',
    dueDate: '',
    estimatedEffort: 3,
    projectId: 'project-1',
    authorId: 'user-1',
    assigneeId: 'user-2',
  };

  function createRoute(id: string | null): ActivatedRoute {
    return {
      snapshot: {
        paramMap: {
          get: vi.fn(() => id),
        },
      },
    } as unknown as ActivatedRoute;
  }

  beforeEach(() => {
    workItemService = {
      getWorkItemById: vi.fn(() => of(task)),
      updateWorkItemStatus: vi.fn(() => of(undefined)),
      assignWorkItem: vi.fn(() => of(undefined)),
    };
    component = new TaskDetailComponent(
      createRoute('task-1'),
      workItemService as unknown as WorkItemService,
    );
  });

  it('reads the task id from route params and loads task details', () => {
    component.ngOnInit();

    expect(workItemService.getWorkItemById).toHaveBeenCalledWith('task-1');
    expect(component.task).toEqual(task);
    expect(component.status).toBe('Active');
    expect(component.assigneeId).toBe('user-2');
  });

  it('does not load a task when the route has no id', () => {
    component = new TaskDetailComponent(
      createRoute(null),
      workItemService as unknown as WorkItemService,
    );

    component.loadTask();

    expect(workItemService.getWorkItemById).not.toHaveBeenCalled();
  });

  it('updates status and reloads the task', () => {
    component.status = 'Resolved';

    component.updateStatus();

    expect(workItemService.updateWorkItemStatus).toHaveBeenCalledWith('task-1', {
      status: 'Resolved',
    });
    expect(workItemService.getWorkItemById).toHaveBeenCalledWith('task-1');
  });

  it('assigns a user and reloads the task', () => {
    component.assigneeId = 'user-3';

    component.assignUser();

    expect(workItemService.assignWorkItem).toHaveBeenCalledWith('task-1', {
      assigneeId: 'user-3',
    });
    expect(workItemService.getWorkItemById).toHaveBeenCalledWith('task-1');
  });

  it('maps statuses to tag severities', () => {
    expect(component.getSeverity('New')).toBe('info');
    expect(component.getSeverity('Active')).toBe('warn');
    expect(component.getSeverity('Resolved')).toBe('success');
    expect(component.getSeverity('Closed')).toBe('secondary');
    expect(component.getSeverity('Unexpected')).toBe('info');
  });
});
