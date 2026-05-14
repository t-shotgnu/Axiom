import { of } from 'rxjs';
import { WorkItem, WorkItemService } from '../../core/services/work-item.service';
import { TasksComponent } from './tasks';

describe('TasksComponent', () => {
  let workItemService: {
    getWorkItems: ReturnType<typeof vi.fn>;
    createWorkItem: ReturnType<typeof vi.fn>;
  };
  let component: TasksComponent;

  const workItems: WorkItem[] = [
    {
      id: 'task-1',
      controlNo: 1,
      description: 'Build tests',
      priority: 2,
      type: 'Task',
      status: 'New',
      dueDate: '',
      estimatedEffort: 3,
      projectId: 'project-1',
      authorId: 'user-1',
      assigneeId: '',
    },
  ];

  beforeEach(() => {
    workItemService = {
      getWorkItems: vi.fn(() => of(workItems)),
      createWorkItem: vi.fn(() => of('task-2')),
    };
    component = new TasksComponent(workItemService as unknown as WorkItemService);
  });

  it('loads tasks for the selected fetch project id', () => {
    component.fetchProjectId = 'project-1';

    component.loadTasks();

    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-1');
    expect(component.tasks).toEqual(workItems);
  });

  it('does not load tasks without a fetch project id', () => {
    component.fetchProjectId = '';

    component.loadTasks();

    expect(workItemService.getWorkItems).not.toHaveBeenCalled();
  });

  it('creates a task and reloads tasks for that project', () => {
    component.projectId = 'project-1';
    component.newTask = {
      description: 'New task',
      priority: 1,
      type: 'Task',
      status: 'New',
    };

    component.createTask();

    expect(workItemService.createWorkItem).toHaveBeenCalledWith({
      description: 'New task',
      priority: 1,
      type: 'Task',
      status: 'New',
      projectId: 'project-1',
    });
    expect(component.newTask.description).toBe('');
    expect(component.fetchProjectId).toBe('project-1');
    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-1');
  });

  it('does not create a task without a project id', () => {
    component.projectId = '';

    component.createTask();

    expect(workItemService.createWorkItem).not.toHaveBeenCalled();
  });
});
