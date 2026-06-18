import { of } from 'rxjs';
import { WorkItem, WorkItemService } from '../../core/services/work-item.service';
import { TasksComponent } from './tasks';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';

describe('TasksComponent', () => {
  let workItemService: {
    getWorkItems: ReturnType<typeof vi.fn>;
    createWorkItem: ReturnType<typeof vi.fn>;
    updateWorkItemStatus: ReturnType<typeof vi.fn>;
    getRelationshipsByProject: ReturnType<typeof vi.fn>;
    getTypeHierarchy: ReturnType<typeof vi.fn>;
  };
  let fixture: ComponentFixture<TasksComponent>;
  let component: TasksComponent;
  let router: { navigate: ReturnType<typeof vi.fn> };

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
      updateWorkItemStatus: vi.fn(() => of(void 0)),
      getRelationshipsByProject: vi.fn(() => of([])),
      getTypeHierarchy: vi.fn(() => of({})),
    };
    router = { navigate: vi.fn(() => Promise.resolve(true)) };

    TestBed.configureTestingModule({
      imports: [TasksComponent],
      providers: [
        { provide: WorkItemService, useValue: workItemService },
        { provide: ActivatedRoute, useValue: { paramMap: { subscribe: () => ({ unsubscribe: () => { } }) } } },
        { provide: Router, useValue: router },
      ],
    });

    fixture = TestBed.createComponent(TasksComponent);
    component = fixture.componentInstance;
    // ensure a current project exists for loadTasks
    (component as any).currentProject = { id: 'project-1' } as any;
  });

  it('loads tasks for the selected fetch project id', () => {
    component.fetchProjectId = 'project-1';

    component.loadTasks();

    expect(workItemService.getWorkItems).toHaveBeenCalledWith('project-1');
    expect(component.tasks).toEqual(workItems);
  });

  it('does not load tasks without a fetch project id', () => {
    (component as any).currentProject = null;

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
    component.fetchProjectId = '';
    (component as any).currentProject = null;

    component.createTask();

    expect(workItemService.createWorkItem).not.toHaveBeenCalled();
  });

  it('passes the selected project when navigating to task creation', () => {
    component.navigateToCreate();

    expect(router.navigate).toHaveBeenCalledWith(['/tasks/new'], {
      queryParams: { projectId: 'project-1' },
    });
  });

  it('moves an issue between board status columns', () => {
    component.tasks = [...workItems];
    component.relationships = [];
    component.applyFiltersAndPagination();

    component.moveTaskToStatus(component.tasks[0], 'Active');

    expect(workItemService.updateWorkItemStatus).toHaveBeenCalledWith('task-1', { status: 'Active' });
    expect(component.tasks[0].status).toBe('Active');
    expect(component.getBoardTasks('Active')).toHaveLength(1);
  });
});
